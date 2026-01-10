package com.uber.server.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WireEncodingTest {
    
    @Test
    void testEncodeDecodeInt32_SmallPositive() {
        int original = 42;
        byte[] encoded = WireEncoding.encodeInt32(original);
        WireEncoding.DecodeResult result = WireEncoding.decodeInt32(encoded);
        assertEquals(original, result.value);
    }
    
    @Test
    void testEncodeDecodeInt32_LargePositive() {
        int original = 1234567;
        byte[] encoded = WireEncoding.encodeInt32(original);
        WireEncoding.DecodeResult result = WireEncoding.decodeInt32(encoded);
        assertEquals(original, result.value);
        assertTrue(result.bytesConsumed > 0);
    }
    
    @Test
    void testEncodeDecodeInt32_Negative() {
        int original = -1234;
        byte[] encoded = WireEncoding.encodeInt32(original);
        WireEncoding.DecodeResult result = WireEncoding.decodeInt32(encoded);
        assertEquals(original, result.value);
    }
    
    @Test
    void testEncodeDecodeInt32_Zero() {
        int original = 0;
        byte[] encoded = WireEncoding.encodeInt32(original);
        WireEncoding.DecodeResult result = WireEncoding.decodeInt32(encoded);
        assertEquals(original, result.value);
    }
    
    @Test
    void testEncodeDecodeInt32_MaxValue() {
        int original = Integer.MAX_VALUE;
        byte[] encoded = WireEncoding.encodeInt32(original);
        WireEncoding.DecodeResult result = WireEncoding.decodeInt32(encoded);
        assertEquals(original, result.value);
    }
    
    @Test
    void testEncodeDecodeInt32_MinValue() {
        int original = Integer.MIN_VALUE;
        byte[] encoded = WireEncoding.encodeInt32(original);
        WireEncoding.DecodeResult result = WireEncoding.decodeInt32(encoded);
        assertEquals(original, result.value);
    }
    
    @Test
    void testDecodeWithArray() {
        int original = 12345;
        byte[] encoded = WireEncoding.encodeInt32(original);
        int[] totalBytesOut = new int[1];
        int decoded = WireEncoding.decodeInt32(encoded, totalBytesOut);
        assertEquals(original, decoded);
        assertTrue(totalBytesOut[0] > 0);
    }
    
    @Test
    void testConstants() {
        assertEquals(72, WireEncoding.NEGATIVE);
        assertEquals(73, WireEncoding.POSITIVE);
        assertEquals(6, WireEncoding.MAX_INTEGER_BYTE_AMOUNT);
    }
}
