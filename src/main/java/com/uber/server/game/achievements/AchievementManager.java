package com.uber.server.game.achievements;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.AchievementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages achievements.
 */
public class AchievementManager {
    private static final Logger logger = LoggerFactory.getLogger(AchievementManager.class);
    private final Map<Long, Achievement> achievements;
    private final AchievementRepository achievementRepository;
    private final Game game;
    
    public AchievementManager(AchievementRepository achievementRepository, Game game) {
        this.achievements = new ConcurrentHashMap<>();
        this.achievementRepository = achievementRepository;
        this.game = game;
    }
    
    /**
     * Loads all achievements from the database.
     */
    public void loadAchievements() {
        achievements.clear();
        
        List<Map<String, Object>> data = achievementRepository.loadAchievements();
        for (Map<String, Object> row : data) {
            try {
                long id = ((Number) row.get("id")).longValue();
                int levels = ((Number) row.get("levels")).intValue();
                String badge = (String) row.get("badge");
                int pixelBase = ((Number) row.get("pixels_base")).intValue();
                double pixelMultiplier = ((Number) row.get("pixels_multiplier")).doubleValue();
                String dynamicBadgeLevelStr = (String) row.get("dynamic_badgelevel");
                boolean dynamicBadgeLevel = "1".equals(dynamicBadgeLevelStr) || 
                                           "true".equalsIgnoreCase(dynamicBadgeLevelStr);
                
                Achievement achievement = new Achievement(id, levels, badge, pixelBase, 
                                                        pixelMultiplier, dynamicBadgeLevel);
                achievements.put(id, achievement);
            } catch (Exception e) {
                logger.error("Failed to load achievement: {}", e.getMessage(), e);
            }
        }
        
        logger.info("Loaded {} achievements", achievements.size());
    }
    
    /**
     * Checks if user has an achievement at the specified level or higher.
     * @param session GameClient session
     * @param achievementId Achievement ID
     * @param minLevel Minimum level
     * @return True if user has the achievement at or above the minimum level
     */
    public boolean userHasAchievement(GameClient session, long achievementId, int minLevel) {
        if (session == null || session.getHabbo() == null) {
            return false;
        }
        
        Habbo habbo = session.getHabbo();
        Map<Long, Integer> userAchievements = habbo.getAchievements();
        
        if (!userAchievements.containsKey(achievementId)) {
            return false;
        }
        
        return userAchievements.get(achievementId) >= minLevel;
    }
    
    /**
     * Serializes achievement list for a user.
     * @param session GameClient session
     * @return ServerMessage with achievement data
     */
    public ServerMessage serializeAchievementList(GameClient session) {
        if (session == null || session.getHabbo() == null) {
            ServerMessage emptyMsg = new ServerMessage(436); // Empty message
            var composer = new com.uber.server.messages.outgoing.users.AchievementsComposer(emptyMsg);
            return composer.compose();
        }
        
        Habbo habbo = session.getHabbo();
        Map<Long, Integer> userAchievements = habbo.getAchievements();
        
        List<Achievement> achievementsToList = new ArrayList<>();
        Map<Long, Integer> nextAchievementLevels = new HashMap<>();
        
        for (Achievement achievement : achievements.values()) {
            if (!userAchievements.containsKey(achievement.getId())) {
                achievementsToList.add(achievement);
                nextAchievementLevels.put(achievement.getId(), 1);
            } else {
                int userLevel = userAchievements.get(achievement.getId());
                if (userLevel >= achievement.getLevels()) {
                    continue; // Achievement completed
                }
                
                achievementsToList.add(achievement);
                nextAchievementLevels.put(achievement.getId(), userLevel + 1);
            }
        }
        
        ServerMessage message = new ServerMessage(436);
        message.appendInt32(achievementsToList.size());
        
        for (Achievement achievement : achievementsToList) {
            int level = nextAchievementLevels.get(achievement.getId());
            message.appendUInt(achievement.getId());
            message.appendInt32(level);
            message.appendStringWithBreak(formatBadgeCode(achievement.getBadgeCode(), 
                                                          level, 
                                                          achievement.isDynamicBadgeLevel()));
        }
        
        // Wrap in composer
        var composer = new com.uber.server.messages.outgoing.users.AchievementsComposer(message);
        return composer.compose();
    }
    
    /**
     * Unlocks an achievement for a user.
     * @param session GameClient session
     * @param achievementId Achievement ID
     * @param level Achievement level
     */
    public void unlockAchievement(GameClient session, long achievementId, int level) {
        if (session == null || session.getHabbo() == null) {
            return;
        }
        
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) {
            return;
        }
        
        Habbo habbo = session.getHabbo();
        Map<Long, Integer> userAchievements = habbo.getAchievements();
        
        // Check if user already has this achievement at this level or higher
        if (userHasAchievement(session, achievementId, level) || 
            level < 1 || level > achievement.getLevels()) {
            return;
        }
        
        // Calculate pixel value for this achievement
        int value = calculateAchievementValue(achievement.getPixelBase(), 
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
            game.getBadgeRepository().removeBadge(habbo.getId(), badge);
        }
        
        // Give the user the new badge
        String newBadgeCode = formatBadgeCode(achievement.getBadgeCode(), level, 
                                             achievement.isDynamicBadgeLevel());
        habbo.getBadgeComponent().giveBadge(newBadgeCode, true);
        game.getBadgeRepository().giveBadge(habbo.getId(), newBadgeCode, 0, true);
        
        // Update or set the achievement level for the user
        userAchievements.put(achievementId, level);
        achievementRepository.updateUserAchievement(habbo.getId(), achievementId, level);
        
        // Notify the user of the achievement gain
        ServerMessage response = new ServerMessage(437);
        response.appendUInt(achievementId);
        response.appendInt32(level);
        response.appendStringWithBreak(newBadgeCode);
        
        if (level > 1) {
            String previousBadgeCode = formatBadgeCode(achievement.getBadgeCode(), level - 1, 
                                                       achievement.isDynamicBadgeLevel());
            response.appendStringWithBreak(previousBadgeCode);
        } else {
            response.appendStringWithBreak("");
        }
        
        var composer = new com.uber.server.messages.outgoing.users.HabboAchievementNotificationComposer(response);
        session.sendMessage(composer.compose());
        
        // Give the user the pixels
        habbo.setActivityPoints(habbo.getActivityPoints() + value);
        habbo.updateActivityPointsBalance(true);
    }
    
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
    
    /**
     * Gets an achievement by ID.
     * @param achievementId Achievement ID
     * @return Achievement object, or null if not found
     */
    public Achievement getAchievement(long achievementId) {
        return achievements.get(achievementId);
    }
    
    /**
     * Gets all achievements.
     * @return Map of achievement ID to Achievement object
     */
    public Map<Long, Achievement> getAchievements() {
        return new HashMap<>(achievements);
    }
}
