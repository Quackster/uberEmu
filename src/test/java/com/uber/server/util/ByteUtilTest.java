package com.uber.server.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ByteUtilTest {
    
    @Test
    void testChompBytes_Normal() {
        byte[] source = {1, 2, 3, 4, 5, 6, 7, 8};
        byte[] result = ByteUtil.chompBytes(source, 2, 3);
        assertArrayEquals(new byte[]{3, 4, 5}, result);
    }
    
    @Test
    void testChompBytes_StartFromZero() {
        byte[] source = {1, 2, 3, 4, 5};
        byte[] result = ByteUtil.chompBytes(source, 0, 3);
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }
    
    @Test
    void testChompBytes_ExceedsLength() {
        byte[] source = {1, 2, 3};
        byte[] result = ByteUtil.chompBytes(source, 1, 5);
        assertArrayEquals(new byte[]{2, 3}, result);
    }
    
    @Test
    void testChompBytes_NegativeOffset() {
        byte[] source = {1, 2, 3};
        byte[] result = ByteUtil.chompBytes(source, -1, 2);
        // Should handle gracefully, may return empty or partial array
        assertNotNull(result);
    }
    
    @Test
    void testChompBytes_EmptyArray() {
        byte[] source = {};
        byte[] result = ByteUtil.chompBytes(source, 0, 5);
        assertEquals(0, result.length);
    }
    
    @Test
    void testChompBytes_NullArray() {
        byte[] result = ByteUtil.chompBytes(null, 0, 5);
        assertEquals(0, result.length);
    }
    
    @Test
    void testChompBytes_ZeroBytes() {
        byte[] source = {1, 2, 3, 4, 5};
        byte[] result = ByteUtil.chompBytes(source, 2, 0);
        assertEquals(0, result.length);
    }
}
