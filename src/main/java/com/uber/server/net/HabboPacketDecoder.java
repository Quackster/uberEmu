package com.uber.server.net;

import com.uber.server.messages.ClientMessage;
import com.uber.server.util.Base64Encoding;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Netty decoder for Habbo protocol packets.
 * Parses packets in format: [3 bytes: Base64 length][2 bytes: Base64 message ID][body]
 * Handles packet fragmentation and batching (multiple messages in one buffer).
 * Ported from C# TcpConnection.DataReceived() and GameClient.handleConnectionData()
 */
public class HabboPacketDecoder extends ReplayingDecoder<Void> {
    private static final Logger logger = LoggerFactory.getLogger(HabboPacketDecoder.class);
    private static final int MIN_PACKET_SIZE = 5; // 3 bytes length + 2 bytes ID
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Check for cross-domain policy request
        // Flash clients send "<policy-file-request/>" which starts with '<' (0x3C)
        // Normal packets start with Base64-encoded length bytes (0x40-0x7F range)
        if (in.readableBytes() > 0) {
            byte firstByte = in.getByte(in.readerIndex());
            // Policy requests typically start with '<' (0x3C) or other ASCII chars
            // Base64 encoding for length starts at 0x40 ('@'), so any byte < 0x40 that's not a control char is likely a policy request
            if (firstByte < 0x40 && firstByte >= 0x20) {
                // This is likely a cross-domain policy request (ASCII text)
                handleCrossDomainPolicy(ctx, in);
                return;
            }
        }
        
        // Parse batched messages until buffer is exhausted
        while (in.readableBytes() >= MIN_PACKET_SIZE) {
            // Mark reader index in case we need to rollback
            in.markReaderIndex();
            
            try {
                // Parse message: [3 bytes: Base64 length][2 bytes: Base64 message ID][body]
                // Decode message length (3 bytes Base64)
                byte[] lengthBytes = new byte[3];
                in.readBytes(lengthBytes);
                int messageLength = Base64Encoding.decodeInt32(lengthBytes);
                
                // Validate message length (must be at least 2 for the ID bytes, reasonable max)
                if (messageLength < 2 || messageLength > 16384) { // Max reasonable size
                    logger.warn("Invalid message length: {} (connection: {})", messageLength, ctx.channel().remoteAddress());
                    break; // Skip invalid packet
                }
                
                // Decode message ID (2 bytes Base64)
                byte[] idBytes = new byte[2];
                in.readBytes(idBytes);
                long messageId = Base64Encoding.decodeUInt32(idBytes);
                
                // Extract message body
                int bodyLength = messageLength - 2;
                if (in.readableBytes() < bodyLength) {
                    // Not enough data yet, reset reader index and wait for more data
                    in.resetReaderIndex();
                    break;
                }
                
                byte[] body = new byte[bodyLength];
                in.readBytes(body);
                
                // Create ClientMessage and add to output list
                ClientMessage message = new ClientMessage(messageId, body);
                out.add(message);
                
            } catch (Exception e) {
                logger.warn("Error decoding packet (connection: {}): {}", ctx.channel().remoteAddress(), e.getMessage());
                // Reset to mark and skip this invalid packet
                in.resetReaderIndex();
                // Skip one byte and try again
                in.skipBytes(1);
                // If we can't read even one byte, break
                if (in.readableBytes() < MIN_PACKET_SIZE) {
                    break;
                }
            }
        }
    }
    
    /**
     * Handles cross-domain policy requests from Flash clients.
     * Ported from C# GameClient.handleConnectionData() cross-domain check
     */
    private void handleCrossDomainPolicy(ChannelHandlerContext ctx, ByteBuf in) {
        // Read the policy request (usually "<policy-file-request/>" or similar)
        // Flash clients need this to establish connections
        try {
            byte[] policyRequest = new byte[in.readableBytes()];
            in.readBytes(policyRequest);
            String request = new String(policyRequest);
            
            logger.debug("Received cross-domain policy request from client {}: {}", ctx.channel().remoteAddress(), request);
            
            // Send cross-domain policy response
            // The policy file allows Flash clients to connect
            String policyResponse = "<?xml version=\"1.0\"?>\r\n" +
                    "<!DOCTYPE cross-domain-policy SYSTEM \"http://www.macromedia.com/xml/dtds/cross-domain-policy.dtd\">\r\n" +
                    "<cross-domain-policy>\r\n" +
                    "<allow-access-from domain=\"*\" to-ports=\"*\" />\r\n" +
                    "</cross-domain-policy>\0";
            
            ByteBuf response = ctx.alloc().buffer(policyResponse.length());
            response.writeBytes(policyResponse.getBytes());
            ctx.writeAndFlush(response);
            
            // Close connection after sending policy (Flash clients reconnect)
            ctx.close();
            
        } catch (Exception e) {
            logger.warn("Error handling cross-domain policy request: {}", e.getMessage());
            ctx.close();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Decoder exception (connection: {}): {}", ctx.channel().remoteAddress(), cause.getMessage());
        ctx.close();
    }
}
