package com.uber.server.game.items;

/**
 * Represents a moodlight preset.
 */
public class MoodlightPreset {
    private String colorCode;
    private int colorIntensity;
    private boolean backgroundOnly;
    
    public MoodlightPreset(String colorCode, int colorIntensity, boolean backgroundOnly) {
        this.colorCode = colorCode;
        this.colorIntensity = colorIntensity;
        this.backgroundOnly = backgroundOnly;
    }
    
    // Getters and setters
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    public int getColorIntensity() { return colorIntensity; }
    public void setColorIntensity(int colorIntensity) { this.colorIntensity = colorIntensity; }
    public boolean isBackgroundOnly() { return backgroundOnly; }
    public void setBackgroundOnly(boolean backgroundOnly) { this.backgroundOnly = backgroundOnly; }
}
