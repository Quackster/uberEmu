package com.uber.server.util;

import java.time.Instant;

/**
 * Time utility functions.
 */
public class TimeUtil {
    /**
     * Gets the current Unix timestamp (seconds since epoch).
     * @return Unix timestamp
     */
    public static long getUnixTimestamp() {
        return Instant.now().getEpochSecond();
    }
}
