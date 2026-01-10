package com.uber.server.navigator;

/**
 * Represents a private/flat category in the navigator.
 * Ported from HabboHotel/Navigators/FlatCat.cs
 */
public class FlatCat {
    private final int id;
    private final String caption;
    private final int minRank;
    
    public FlatCat(int id, String caption, int minRank) {
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
