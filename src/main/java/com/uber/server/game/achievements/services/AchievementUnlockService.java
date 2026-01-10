package com.uber.server.game.achievements.services;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.achievements.Achievement;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.AchievementRepository;
import com.uber.server.repository.BadgeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for unlocking achievements for users.
 * Extracted from AchievementManager.
 */
public class AchievementUnlockService {
    private static final Logger logger = LoggerFactory.getLogger(AchievementUnlockService.class);
    
    private final AchievementRepository achievementRepository;
    private final BadgeRepository badgeRepository;
    private final Game game;
    private final AchievementBadgeFormatService badgeFormatService;
    private final AchievementValueService valueService;
    
    public AchievementUnlockService(AchievementRepository achievementRepository,
                                   BadgeRepository badgeRepository,
                                   Game game) {
        this.achievementRepository = achievementRepository;
        this.badgeRepository = badgeRepository;
        this.game = game;
        this.badgeFormatService = new AchievementBadgeFormatService();
        this.valueService = new AchievementValueService();
    }
    
    /**
     * Unlocks an achievement for a user.
     * @param session GameClient session
     * @param achievementId Achievement ID
     * @param level Achievement level
     * @param achievement Achievement object
     */
    public void unlockAchievement(GameClient session, long achievementId, int level, Achievement achievement) {
        if (session == null || session.getHabbo() == null || achievement == null) {
            return;
        }
        
        Habbo habbo = session.getHabbo();
        Map<Long, Integer> userAchievements = habbo.getAchievements();
        
        // Check if user already has this achievement at this level or higher
        if (userAchievements.containsKey(achievementId) && 
            userAchievements.get(achievementId) >= level) {
            return;
        }
        
        if (level < 1 || level > achievement.getLevels()) {
            return;
        }
        
        // Calculate pixel value for this achievement
        int value = valueService.calculateAchievementValue(
            achievement.getPixelBase(), 
            achievement.getPixelMultiplier(), 
            level);
        
        // Remove any previous badges for this achievement (old levels)
        List<String> badgesToRemove = new ArrayList<>();
        for (com.uber.server.game.users.badges.Badge badge : habbo.getBadgeComponent().getBadgeList()) {
            String badgeCode = badge.getCode();
            if (badgeCode != null && badgeCode.startsWith(achievement.getBadgeCode())) {
                badgesToRemove.add(badgeCode);
            }
        }
        
        for (String badge : badgesToRemove) {
            habbo.getBadgeComponent().removeBadge(badge);
            badgeRepository.removeBadge(habbo.getId(), badge);
        }
        
        // Give the user the new badge
        String newBadgeCode = badgeFormatService.formatBadgeCode(
            achievement.getBadgeCode(), level, achievement.isDynamicBadgeLevel());
        habbo.getBadgeComponent().giveBadge(newBadgeCode, true);
        badgeRepository.giveBadge(habbo.getId(), newBadgeCode, 0, true);
        
        // Update or set the achievement level for the user
        userAchievements.put(achievementId, level);
        achievementRepository.updateUserAchievement(habbo.getId(), achievementId, level);
        
        // Notify the user of the achievement gain
        ServerMessage response = new ServerMessage(437);
        response.appendUInt(achievementId);
        response.appendInt32(level);
        response.appendStringWithBreak(newBadgeCode);
        
        if (level > 1) {
            String previousBadgeCode = badgeFormatService.formatBadgeCode(
                achievement.getBadgeCode(), level - 1, achievement.isDynamicBadgeLevel());
            response.appendStringWithBreak(previousBadgeCode);
        } else {
            response.appendStringWithBreak("");
        }
        
        session.sendMessage(response);
        
        // Give the user the pixels
        habbo.setActivityPoints(habbo.getActivityPoints() + value);
        habbo.updateActivityPointsBalance(true);
    }
}
