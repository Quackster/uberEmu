package com.uber.server.net.netty.codec;

import com.uber.server.messages.ServerMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Netty encoder for Habbo protocol packets.
 * Encodes ServerMessage to bytes in format: [2 bytes: Base64 ID][body][1 byte: terminator 0x01]
 */
public class HabboPacketEncoder extends MessageToByteEncoder<Object> {
    private static final Logger logger = LoggerFactory.getLogger(HabboPacketEncoder.class);
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf out) throws Exception {
        if (obj == null) {
            return;
        }

        try {
            byte[] messageBytes = null;

            if (obj instanceof ServerMessage msg)
                messageBytes = msg.getBytes();

            if (obj instanceof String msgStr)
                messageBytes = msgStr.getBytes(StandardCharsets.UTF_8);

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
