package com.uber.server.game.navigator;

/**
 * Represents a private room category in the navigator.
 */
public class RoomCategory {
    private final int id;
    private final String caption;
    private final int minRank;
    
    public RoomCategory(int id, String caption, int minRank) {
        this.id = id;
        this.caption = caption;
        this.minRank = minRank;
    }
    
    public int getId() {
        return id;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public int getMinRank() {
        return minRank;
    }
}
