package com.uber.server.encoding.client;

import com.uber.server.encoding.base64.Base64Encoding;
import com.uber.server.encoding.wire.WireEncoding;
import com.uber.server.messages.ClientMessage;

import java.nio.charset.Charset;

/**
 * Decoder for client messages.
 * Handles decoding of various data types from ClientMessage.
 * Extracted from ClientMessage class.
 */
public class ClientMessageDecoder {
    private static final Charset DEFAULT_ENCODING = Charset.defaultCharset();
    
    private final ClientMessage message;
    
    public ClientMessageDecoder(ClientMessage message) {
        this.message = message;
    }
    
    /**
     * Reads a fixed-length value (length-prefixed with 2-byte Base64 length).
     * @return The value bytes
     */
    public byte[] readFixedValue() {
        byte[] lengthBytes = message.readBytes(2);
        if (lengthBytes.length < 2) {
            return new byte[0];
        }
        int len = Base64Encoding.decodeInt32(lengthBytes);
        return message.readBytes(len);
    }
    
    /**
     * Pops a Base64-encoded boolean.
     */
    public boolean popBase64Boolean() {
        // Access body directly through reflection or use a package-private method
        // For now, use readBytes which advances pointer
        byte[] data = message.readBytes(1);
        if (data.length > 0 && data[0] == Base64Encoding.POSITIVE) {
            return true;
        }
        return false;
    }
    
    /**
     * Pops a Base64-encoded int32.
     */
    public int popInt32() {
        byte[] bytes = message.readBytes(2);
        if (bytes.length < 2) {
            return 0;
        }
        return Base64Encoding.decodeInt32(bytes);
    }
    
    /**
     * Pops a Base64-encoded uint32.
     */
    public long popUInt32() {
        return Integer.toUnsignedLong(popInt32());
    }
    
    /**
     * Pops a fixed-length string.
     */
    public String popFixedString() {
        return popFixedString(DEFAULT_ENCODING);
    }
    
    /**
     * Pops a fixed-length string with specified encoding.
     */
    public String popFixedString(Charset encoding) {
        byte[] value = readFixedValue();
        String result = new String(value, encoding);
        return result.replace('\u0001', ' '); // Replace char 1 with space
    }
    
    /**
     * Pops a fixed-length int32 (as ASCII string).
     */
    public int popFixedInt32() {
        String s = popFixedString(Charset.forName("ASCII"));
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Pops a fixed-length uint32 (as ASCII string).
     */
    public long popFixedUInt32() {
        return Integer.toUnsignedLong(popFixedInt32());
    }
    
    /**
     * Pops a wire-encoded boolean.
     */
    public boolean popWiredBoolean() {
        byte[] data = message.readBytes(1);
        if (data.length > 0 && data[0] == WireEncoding.POSITIVE) {
            return true;
        }
        return false;
    }
    
    /**
     * Pops a wire-encoded int32.
     */
    public int popWiredInt32() {
        if (message.getRemainingLength() < 1) {
            return 0;
        }
        
        byte[] data = message.plainReadBytes(WireEncoding.MAX_INTEGER_BYTE_AMOUNT);
        int[] totalBytesOut = new int[1];
        int result = WireEncoding.decodeInt32(data, totalBytesOut);
        
        message.advancePointer(totalBytesOut[0]);
        
        return result;
    }
    
    /**
     * Pops a wire-encoded uint.
     */
    public long popWiredUInt() {
        return Integer.toUnsignedLong(popWiredInt32());
    }
}
