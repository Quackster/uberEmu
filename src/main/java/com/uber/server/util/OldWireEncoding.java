package com.uber.server.util;

/**
 * Old wire encoding utilities for Habbo protocol (VL64 string-based encoding).
 * Used for parsing static furni maps in room models.
 */
public final class OldWireEncoding {
    private OldWireEncoding() {
        // Utility class
    }
    
    /**
     * Encodes an integer into VL64 string format.
     * @param i The integer to encode
     * @return Encoded string
     */
    public static String encodeVL64(int i) {
        byte[] workBuffer = new byte[6];
        int pos = 0;
        int startPos = pos;
        int numBytes = 1;
        int negativeMask = i >= 0 ? 0 : 4;
        int absValue = Math.abs(i);
        
        workBuffer[pos++] = (byte) (64 + (absValue & 3));
        
        for (absValue >>= 2; absValue != 0; absValue >>= 6) {
            numBytes++;
            workBuffer[pos++] = (byte) (64 + (absValue & 0x3f));
        }
        
        workBuffer[startPos] = (byte) (workBuffer[startPos] | (numBytes << 3) | negativeMask);
        
        // Convert to string, removing null bytes
        StringBuilder result = new StringBuilder();
        for (int j = 0; j < numBytes; j++) {
            if (workBuffer[j] != 0) {
                result.append((char) workBuffer[j]);
            }
        }
        return result.toString();
    }
    
    /**
     * Decodes a VL64-encoded integer from a string.
     * @param data The string containing the encoded integer
     * @return Decoded integer
     */
    public static int decodeVL64(String data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        return decodeVL64(data.toCharArray());
    }
    
    /**
     * Decodes a VL64-encoded integer from a character array.
     * @param raw The character array containing the encoded integer
     * @return Decoded integer
     */
    public static int decodeVL64(char[] raw) {
        try {
            if (raw == null || raw.length == 0) {
                return 0;
            }
            
            int pos = 0;
            int value = 0;
            boolean negative = (raw[pos] & 4) == 4;
            int totalBytes = (raw[pos] >> 3) & 7;
            value = raw[pos] & 3;
            pos++;
            
            int shiftAmount = 2;
            for (int b = 1; b < totalBytes; b++) {
                if (pos >= raw.length) {
                    break;
                }
                value |= ((raw[pos] & 0x3f) << shiftAmount);
                shiftAmount = 2 + 6 * b;
                pos++;
            }
            
            if (negative) {
                value = -value;
            }
            
            return value;
        } catch (Exception e) {
            return 0;
        }
    }
}
