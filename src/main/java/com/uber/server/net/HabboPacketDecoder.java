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
        // Check for cross-domain policy request (data[0] != 64)
        // Skip this check for now as it's handled in GameClient
        
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
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Decoder exception (connection: {}): {}", ctx.channel().remoteAddress(), cause.getMessage());
        ctx.close();
    }
}
