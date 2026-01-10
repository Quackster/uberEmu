package com.uber.server.game.achievements;

/**
 * Represents an achievement definition.
 */
public class Achievement {
    private final long id;
    private final int levels;
    private final String badgeCode;
    private final int pixelBase;
    private final double pixelMultiplier;
    private final boolean dynamicBadgeLevel;
    
    public Achievement(long id, int levels, String badgeCode, int pixelBase, 
                      double pixelMultiplier, boolean dynamicBadgeLevel) {
        this.id = id;
        this.levels = levels;
        this.badgeCode = badgeCode;
        this.pixelBase = pixelBase;
        this.pixelMultiplier = pixelMultiplier;
        this.dynamicBadgeLevel = dynamicBadgeLevel;
    }
    
    // Getters
    public long getId() { return id; }
    public int getLevels() { return levels; }
    public String getBadgeCode() { return badgeCode; }
    public int getPixelBase() { return pixelBase; }
    public double getPixelMultiplier() { return pixelMultiplier; }
    public boolean isDynamicBadgeLevel() { return dynamicBadgeLevel; }
}
