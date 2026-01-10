package com.uber.server.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base64EncodingTest {
    
    @Test
    void testEncodeDecodeInt32_2Bytes() {
        int original = 1234;
        byte[] encoded = Base64Encoding.encodeInt32(original, 2);
        int decoded = Base64Encoding.decodeInt32(encoded);
        assertEquals(original, decoded);
    }
    
    @Test
    void testEncodeDecodeInt32_3Bytes() {
        int original = 123456;
        byte[] encoded = Base64Encoding.encodeInt32(original, 3);
        int decoded = Base64Encoding.decodeInt32(encoded);
        assertEquals(original, decoded);
    }
    
    @Test
    void testEncodeDecodeZero() {
        int original = 0;
        byte[] encoded = Base64Encoding.encodeInt32(original, 2);
        int decoded = Base64Encoding.decodeInt32(encoded);
        assertEquals(original, decoded);
    }
    
    @Test
    void testEncodeDecodeNegative() {
        int original = -1234;
        byte[] encoded = Base64Encoding.encodeInt32(original, 2);
        int decoded = Base64Encoding.decodeInt32(encoded);
        // Note: Base64Encoding may have limitations with negative values,
        // so this test may need adjustment based on actual implementation behavior
        assertEquals(original, decoded);
    }
    
    @Test
    void testEncodeUInt32() {
        long original = 4294967295L; // Max uint32
        byte[] encoded = Base64Encoding.encodeUInt32(original, 4);
        long decoded = Base64Encoding.decodeUInt32(encoded);
        assertEquals(original, decoded);
    }
    
    @Test
    void testConstants() {
        assertEquals(64, Base64Encoding.NEGATIVE);
        assertEquals(65, Base64Encoding.POSITIVE);
    }
}
