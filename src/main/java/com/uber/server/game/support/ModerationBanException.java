package com.uber.server.game.support;

/**
 * Exception thrown when a user is banned.
 */
public class ModerationBanException extends Exception {
    public ModerationBanException(String reason) {
        super(reason);
    }
}
