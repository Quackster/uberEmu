package com.uber.server.encoding.wire;

/**
 * Wire encoding utilities for Habbo protocol variable-length integers.
 * Uses a custom encoding scheme for efficient integer representation.
 */
public final class WireEncoding {
    public static final byte NEGATIVE = 72;
    public static final byte POSITIVE = 73;
    public static final int MAX_INTEGER_BYTE_AMOUNT = 6;
    
    private WireEncoding() {
        // Utility class
    }
    
    /**
     * Encodes an integer into a variable-length wire format.
     * @param i The integer to encode
     * @return Encoded byte array (variable length, 1-6 bytes)
     */
    public static byte[] encodeInt32(int i) {
        byte[] workBuffer = new byte[MAX_INTEGER_BYTE_AMOUNT];
        
        int pos = 0;
        int numBytes = 1;
        int startPos = pos;
        int negativeMask = i >= 0 ? 0 : 4;
        
        int absValue = Math.abs(i);
        
        workBuffer[pos++] = (byte) (64 + (absValue & 3));
        
        for (absValue >>= 2; absValue != 0; absValue >>= 6) {
            numBytes++;
            workBuffer[pos++] = (byte) (64 + (absValue & 0x3f));
        }
        
        workBuffer[startPos] = (byte) (workBuffer[startPos] | (numBytes << 3) | negativeMask);
        
        byte[] result = new byte[numBytes];
        System.arraycopy(workBuffer, 0, result, 0, numBytes);
        
        return result;
    }
    
    /**
     * Decodes a wire-encoded integer from a byte array.
     * @param data The byte array containing the encoded integer
     * @param totalBytesOut Output parameter - number of bytes consumed
     * @return Decoded integer
     */
    public static int decodeInt32(byte[] data, int[] totalBytesOut) {
        if (data == null || data.length == 0) {
            totalBytesOut[0] = 0;
            return 0;
        }
        
        int pos = 0;
        int value = 0;
        
        boolean negative = (data[pos] & 4) == 4;
        int totalBytes = (data[pos] >> 3) & 7;
        value = data[pos] & 3;
        
        pos++;
        
        int shiftAmount = 2;
        
        for (int b = 1; b < totalBytes; b++) {
            if (pos >= data.length) {
                break;
            }
            value |= ((data[pos] & 0x3f) << shiftAmount);
            shiftAmount = 2 + 6 * b;
            pos++;
        }
        
        if (negative) {
            value = -value;
        }
        
        totalBytesOut[0] = totalBytes;
        return value;
    }
    
    /**
     * Decodes a wire-encoded integer from a byte array (convenience method).
     * @param data The byte array containing the encoded integer
     * @return Result object containing decoded integer and bytes consumed
     */
    public static DecodeResult decodeInt32(byte[] data) {
        int[] totalBytes = new int[1];
        int value = decodeInt32(data, totalBytes);
        return new DecodeResult(value, totalBytes[0]);
    }
    
    /**
     * Result object for decode operations.
     */
    public static class DecodeResult {
        public final int value;
        public final int bytesConsumed;
        
        public DecodeResult(int value, int bytesConsumed) {
            this.value = value;
            this.bytesConsumed = bytesConsumed;
        }
    }
}
