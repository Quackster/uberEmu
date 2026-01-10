package com.uber.server.util;

/**
 * Byte array utility functions.
 */
public final class ByteUtil {
    
    private ByteUtil() {
        // Utility class
    }
    
    /**
     * Extracts a chunk of bytes from a byte array.
     * @param bytes The source byte array
     * @param offset The offset to start from
     * @param numBytes The number of bytes to extract
     * @return A new byte array containing the extracted bytes
     */
    public static byte[] chompBytes(byte[] bytes, int offset, int numBytes) {
        if (bytes == null || bytes.length == 0) {
            return new byte[0];
        }
        
        int end = offset + numBytes;
        if (end > bytes.length) {
            end = bytes.length;
        }
        
        if (numBytes > bytes.length) {
            numBytes = bytes.length;
        }
        if (numBytes < 0) {
            numBytes = 0;
        }
        
        // Adjust numBytes based on available data
        int availableBytes = bytes.length - offset;
        if (availableBytes < numBytes) {
            numBytes = Math.max(0, availableBytes);
        }
        
        byte[] chunk = new byte[numBytes];
        for (int x = 0; x < numBytes; x++) {
            if (offset + x < bytes.length) {
                chunk[x] = bytes[offset + x];
            }
        }
        
        return chunk;
    }
}
