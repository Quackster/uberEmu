package com.uber.server.messages;

import com.uber.server.util.Base64Encoding;
import com.uber.server.util.WireEncoding;

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
     * Reads a fixed-length value (length-prefixed with 2-byte Base64 length).
     * @return The value bytes
     */
    public byte[] readFixedValue() {
        byte[] lengthBytes = readBytes(2);
        if (lengthBytes.length < 2) {
            return new byte[0];
        }
        int len = Base64Encoding.decodeInt32(lengthBytes);
        return readBytes(len);
    }
    
    public boolean popBase64Boolean() {
        if (getRemainingLength() > 0 && body[pointer++] == Base64Encoding.POSITIVE) {
            return true;
        }
        return false;
    }
    
    public int popInt32() {
        byte[] bytes = readBytes(2);
        if (bytes.length < 2) {
            return 0;
        }
        return Base64Encoding.decodeInt32(bytes);
    }
    
    public long popUInt32() {
        return Integer.toUnsignedLong(popInt32());
    }
    
    public String popFixedString() {
        return popFixedString(DEFAULT_ENCODING);
    }
    
    public String popFixedString(Charset encoding) {
        byte[] value = readFixedValue();
        String result = new String(value, encoding);
        return result.replace('\u0001', ' '); // Replace char 1 with space
    }
    
    public int popFixedInt32() {
        String s = popFixedString(Charset.forName("ASCII"));
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public long popFixedUInt32() {
        return Integer.toUnsignedLong(popFixedInt32());
    }
    
    public boolean popWiredBoolean() {
        if (getRemainingLength() > 0 && body[pointer++] == WireEncoding.POSITIVE) {
            return true;
        }
        return false;
    }
    
    public int popWiredInt32() {
        if (getRemainingLength() < 1) {
            return 0;
        }
        
        byte[] data = plainReadBytes(WireEncoding.MAX_INTEGER_BYTE_AMOUNT);
        int[] totalBytesOut = new int[1];
        int result = WireEncoding.decodeInt32(data, totalBytesOut);
        
        pointer += totalBytesOut[0];
        
        return result;
    }
    
    public long popWiredUInt() {
        return Integer.toUnsignedLong(popWiredInt32());
    }
    
    @Override
    public String toString() {
        return getHeader() + getBody();
    }
}
