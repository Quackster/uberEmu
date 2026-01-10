package com.uber.server.net;

import com.uber.server.messages.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty encoder for Habbo protocol packets.
 * Encodes ServerMessage to bytes in format: [2 bytes: Base64 ID][body][1 byte: terminator 0x01]
 * Ported from C# ServerMessage.GetBytes()
 */
public class HabboPacketEncoder extends MessageToByteEncoder<ServerMessage> {
    private static final Logger logger = LoggerFactory.getLogger(HabboPacketEncoder.class);
    
    @Override
    protected void encode(ChannelHandlerContext ctx, ServerMessage msg, ByteBuf out) throws Exception {
        if (msg == null) {
            return;
        }
        
        try {
            byte[] messageBytes = msg.getBytes();
            out.writeBytes(messageBytes);
        } catch (Exception e) {
            logger.error("Error encoding message: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Encoder exception (connection: {}): {}", ctx.channel().remoteAddress(), cause.getMessage());
        ctx.close();
    }
}
