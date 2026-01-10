package com.uber.server.game.support;

/**
 * Exception thrown when a user is banned.
 * Ported from HabboHotel/Support/ModerationBanException.cs
 */
public class ModerationBanException extends Exception {
    public ModerationBanException(String reason) {
        super(reason);
    }
}
