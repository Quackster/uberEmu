package com.uber.server.encoding.server;

import com.uber.server.encoding.base64.Base64Encoding;
import com.uber.server.encoding.wire.WireEncoding;
import com.uber.server.messages.ServerMessage;

import java.nio.charset.Charset;

/**
 * Encoder for server messages.
 * Handles encoding of ServerMessage to byte arrays.
 * Extracted from ServerMessage class.
 */
public class ServerMessageEncoder {
    private static final Charset DEFAULT_ENCODING = Charset.defaultCharset();
    
    private final ServerMessage message;
    
    public ServerMessageEncoder(ServerMessage message) {
        this.message = message;
    }
    
    /**
     * Converts the message to byte array format: [2 bytes: Base64 ID][body][1 byte: terminator]
     * @return Byte array ready to send over TCP
     */
    public byte[] encode() {
        byte[] header = Base64Encoding.encodeUInt32(message.getId(), 2);
        byte[] data = new byte[message.getLength() + 3];
        
        data[0] = header[0];
        data[1] = header[1];
        
        for (int i = 0; i < message.getLength(); i++) {
            data[i + 2] = message.getBodyByte(i);
        }
        
        data[data.length - 1] = 1; // Terminator
        
        return data;
    }
    
    /**
     * Gets the header bytes for the message.
     */
    public byte[] getHeaderBytes() {
        return Base64Encoding.encodeUInt32(message.getId(), 2);
    }
}
