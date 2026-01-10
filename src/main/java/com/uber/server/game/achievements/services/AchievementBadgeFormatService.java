package com.uber.server.game.achievements.services;

/**
 * Service for formatting achievement badge codes.
 * Extracted from AchievementManager.
 */
public class AchievementBadgeFormatService {
    
    /**
     * Formats a badge code with level if dynamic.
     * @param badgeTemplate Badge template code
     * @param level Achievement level
     * @param dynamic Whether badge level is dynamic
     * @return Formatted badge code
     */
    public String formatBadgeCode(String badgeTemplate, int level, boolean dynamic) {
        if (!dynamic) {
            return badgeTemplate;
        }
        
        return badgeTemplate + level;
    }
}
