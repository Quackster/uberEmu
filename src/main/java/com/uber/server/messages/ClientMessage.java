package com.uber.server.messages;

import com.uber.server.encoding.base64.Base64Encoding;
import com.uber.server.encoding.client.ClientMessageDecoder;
import com.uber.server.encoding.wire.WireEncoding;

import java.nio.charset.Charset;

/**
 * Client-to-server message parser.
 * Parses messages in format: [2 bytes: Base64 message ID][body]
 */
public class ClientMessage {
    private final long messageId;
    private final byte[] body;
    private int pointer;
    private static final Charset DEFAULT_ENCODING = Charset.defaultCharset();
    private ClientMessageDecoder decoder;
    
    public ClientMessage(long messageId, byte[] body) {
        this.messageId = messageId;
        this.body = body != null ? body : new byte[0];
        this.pointer = 0;
    }
    
    public long getId() {
        return messageId;
    }
    
    public int getLength() {
        return body.length;
    }
    
    public int getRemainingLength() {
        return body.length - pointer;
    }
    
    public String getHeader() {
        byte[] headerBytes = Base64Encoding.encodeUInt32(messageId, 2);
        return new String(headerBytes, DEFAULT_ENCODING);
    }
    
    public void resetPointer() {
        pointer = 0;
    }
    
    public void advancePointer(int amount) {
        pointer += amount;
        if (pointer > body.length) {
            pointer = body.length;
        }
    }
    
    public String getBody() {
        return new String(body, DEFAULT_ENCODING);
    }
    
    /**
     * Reads bytes and advances the pointer.
     * @param bytes Number of bytes to read
     * @return Byte array containing the read bytes
     */
    public byte[] readBytes(int bytes) {
        if (bytes > getRemainingLength()) {
            bytes = getRemainingLength();
        }
        
        byte[] data = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            if (pointer < body.length) {
                data[i] = body[pointer++];
            }
        }
        
        return data;
    }
    
    /**
     * Reads bytes without advancing the pointer.
     * @param bytes Number of bytes to read
     * @return Byte array containing the read bytes
     */
    public byte[] plainReadBytes(int bytes) {
        if (bytes > getRemainingLength()) {
            bytes = getRemainingLength();
        }
        
        byte[] data = new byte[bytes];
        for (int x = 0, y = pointer; x < bytes && y < body.length; x++, y++) {
            data[x] = body[y];
        }
        
        return data;
    }
    
    /**
     * Gets the decoder for this message.
     * @return ClientMessageDecoder instance
     */
    public ClientMessageDecoder getDecoder() {
        if (decoder == null) {
            decoder = new ClientMessageDecoder(this);
        }
        return decoder;
    }
    
    /**
     * Reads a fixed-length value (length-prefixed with 2-byte Base64 length).
     * Delegates to ClientMessageDecoder.
     */
    public byte[] readFixedValue() {
        return getDecoder().readFixedValue();
    }
    
    public boolean popBase64Boolean() {
        return getDecoder().popBase64Boolean();
    }
    
    public int popInt32() {
        return getDecoder().popInt32();
    }
    
    public long popUInt32() {
        return getDecoder().popUInt32();
    }
    
    public String popFixedString() {
        return getDecoder().popFixedString();
    }
    
    public String popFixedString(Charset encoding) {
        return getDecoder().popFixedString(encoding);
    }
    
    public int popFixedInt32() {
        return getDecoder().popFixedInt32();
    }
    
    public long popFixedUInt32() {
        return getDecoder().popFixedUInt32();
    }
    
    public boolean popWiredBoolean() {
        return getDecoder().popWiredBoolean();
    }
    
    public int popWiredInt32() {
        return getDecoder().popWiredInt32();
    }
    
    public long popWiredUInt() {
        return getDecoder().popWiredUInt();
    }
    
    @Override
    public String toString() {
        return getHeader() + getBody();
    }
}
