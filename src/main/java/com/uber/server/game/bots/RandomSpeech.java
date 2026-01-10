package com.uber.server.game.bots;

/**
 * Represents a random speech entry for a bot.
 */
public class RandomSpeech {
    private final String message;
    private final boolean shout;
    
    public RandomSpeech(String message, boolean shout) {
        this.message = message;
        this.shout = shout;
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean isShout() {
        return shout;
    }
}
