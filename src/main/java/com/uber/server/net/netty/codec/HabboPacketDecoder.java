package com.uber.server.net.netty.codec;

import com.uber.server.messages.ClientMessage;
import com.uber.server.encoding.base64.Base64Encoding;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Netty decoder for Habbo protocol packets.
 * Parses packets in format: [3 bytes: Base64 length][2 bytes: Base64 message ID][body]
 * Handles packet fragmentation and batching (multiple messages in one buffer).
 * Based on Havana NetworkDecoder implementation.
 */
public class HabboPacketDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LogManager.getLogger(HabboPacketDecoder.class);
    private static final int MIN_PACKET_SIZE = 5; // 3 bytes length + 2 bytes ID
    private static final int MAX_MESSAGE_LENGTH = 65536; // Maximum reasonable message size (64KB)
    private static final int MAX_BODY_LENGTH = MAX_MESSAGE_LENGTH - 2; // Account for 2-byte ID
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Check minimum packet size
        if (in.readableBytes() < MIN_PACKET_SIZE) {
            return; // Not enough data yet
        }
        
        // Check for cross-domain policy request (Flash clients)
        // Policy requests start with '<' (0x3C) or other non-Base64 characters
        // Base64 encoding starts at 0x40 ('@'), so bytes < 0x40 are likely policy requests
        in.markReaderIndex();
        
        byte firstByte = in.readByte();
        if (firstByte != 64) {
            // This is a cross-domain policy request
            in.resetReaderIndex();
            handleCrossDomainPolicy(ctx, in);
            return;
        }
        
        in.resetReaderIndex();
        
        // Parse batched messages until buffer is exhausted
        while (in.readableBytes() >= MIN_PACKET_SIZE) {
            in.markReaderIndex();
            
            try {
                // Parse message: [3 bytes: Base64 length][2 bytes: Base64 message ID][body]
                // Decode message length (3 bytes Base64)
                byte[] lengthBytes = new byte[3];
                in.readBytes(lengthBytes);
                int messageLength = Base64Encoding.decodeInt32(lengthBytes);
                
                // Validate message length (must be at least 2 for the ID bytes, reasonable max)
                // Check for negative values and integer overflow issues
                if (messageLength < 2 || messageLength > MAX_MESSAGE_LENGTH) {
                    logger.warn("Invalid message length: {} (connection: {})", messageLength, ctx.channel().remoteAddress());
                    break; // Skip invalid packet
                }
                
                // Check if we have enough data in buffer for the full message
                if (in.readableBytes() < messageLength) {
                    // Not enough data yet, reset reader index and wait for more data
                    in.resetReaderIndex();
                    return;
                }
                
                // Decode message ID (2 bytes Base64)
                byte[] idBytes = new byte[2];
                in.readBytes(idBytes);
                long messageId = Base64Encoding.decodeUInt32(idBytes);
                
                // Extract message body (remaining bytes after ID)
                int bodyLength = messageLength - 2;
                
                // Validate body length before allocation (prevent integer underflow and excessive allocation)
                if (bodyLength < 0 || bodyLength > MAX_BODY_LENGTH) {
                    logger.warn("Invalid body length: {} (messageLength: {}) (connection: {})", 
                        bodyLength, messageLength, ctx.channel().remoteAddress());
                    break; // Skip invalid packet
                }
                
                // Allocate body array (now safe - bodyLength is validated)
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
                if (in.readableBytes() > 0) {
                    in.skipBytes(1);
                }
                // If we can't read even one byte, break
                if (in.readableBytes() < MIN_PACKET_SIZE) {
                    break;
                }
            }
        }
    }
    
    /**
     * Handles cross-domain policy requests from Flash clients.
     * Based on Havana NetworkDecoder implementation.
     */
    private void handleCrossDomainPolicy(ChannelHandlerContext ctx, ByteBuf in) {
        try {
            int readableBytes = in.readableBytes();
            // Limit policy request size to prevent excessive memory allocation
            // Policy requests are typically very short (< 100 bytes)
            if (readableBytes > 1024) {
                logger.warn("Policy request too large: {} bytes (connection: {})", 
                    readableBytes, ctx.channel().remoteAddress());
                ctx.close();
                return;
            }
            
            // Read and discard the policy request
            in.skipBytes(readableBytes);
            
            // Send cross-domain policy response
            // The policy file allows Flash clients to connect
            String policyResponse = """
                <?xml version="1.0"?>\r
                <!DOCTYPE cross-domain-policy SYSTEM "/xml/dtds/cross-domain-policy.dtd">\r
                <cross-domain-policy>\r
                <allow-access-from domain="*" to-ports="*" />\r
                </cross-domain-policy>\0""";
            
            ctx.channel().writeAndFlush(policyResponse);
            ctx.close();

        } catch (Exception e) {
            logger.warn("Error handling cross-domain policy request: {}", e.getMessage());
            ctx.close();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.warn("Decoder exception (connection: {}): {}", ctx.channel().remoteAddress(), cause);
       //  ctx.close();
    }
}
