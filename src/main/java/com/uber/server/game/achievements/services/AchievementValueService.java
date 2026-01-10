package com.uber.server.game.achievements.services;

/**
 * Service for calculating achievement values.
 * Extracted from AchievementManager.
 */
public class AchievementValueService {
    
    /**
     * Calculates the pixel value for an achievement level.
     * @param baseValue Base pixel value
     * @param multiplier Multiplier (not used in original)
     * @param level Achievement level
     * @return Pixel value
     */
    public int calculateAchievementValue(int baseValue, double multiplier, int level) {
        return baseValue + (50 * level);
    }
}
