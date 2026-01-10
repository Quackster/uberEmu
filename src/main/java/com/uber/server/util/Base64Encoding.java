package com.uber.server.util;

/**
 * Base64-like encoding utilities for Habbo protocol.
 * Encodes integers into Base64-like byte sequences.
 */
public final class Base64Encoding {
    public static final byte NEGATIVE = 64;
    public static final byte POSITIVE = 65;
    
    private Base64Encoding() {
        // Utility class
    }
    
    /**
     * Encodes an integer into a Base64-like byte array.
     * @param i The integer to encode
     * @param numBytes Number of bytes to use for encoding
     * @return Encoded byte array
     */
    public static byte[] encodeInt32(int i, int numBytes) {
        byte[] result = new byte[numBytes];
        for (int j = 1; j <= numBytes; j++) {
            int k = (numBytes - j) * 6;
            result[j - 1] = (byte) (0x40 + ((i >> k) & 0x3f));
        }
        return result;
    }
    
    /**
     * Encodes an unsigned integer into a Base64-like byte array.
     * @param i The unsigned integer to encode
     * @param numBytes Number of bytes to use for encoding
     * @return Encoded byte array
     */
    public static byte[] encodeUInt32(long i, int numBytes) {
        return encodeInt32((int) i, numBytes);
    }
    
    /**
     * Decodes a Base64-like byte array into an integer.
     * @param data The byte array to decode
     * @return Decoded integer
     */
    public static int decodeInt32(byte[] data) {
        int result = 0;
        int j = 0;
        for (int k = data.length - 1; k >= 0; k--) {
            int x = (data[k] & 0xFF) - 0x40;
            if (j > 0) {
                x *= (int) Math.pow(64.0, j);
            }
            result += x;
            j++;
        }
        return result;
    }
    
    /**
     * Decodes a Base64-like byte array into an unsigned integer.
     * @param data The byte array to decode
     * @return Decoded unsigned integer as long
     */
    public static long decodeUInt32(byte[] data) {
        return Integer.toUnsignedLong(decodeInt32(data));
    }
}
