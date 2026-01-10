package com.uber.server.game.achievements.services;

import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.achievements.Achievement;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for serializing achievement data for clients.
 * Extracted from AchievementManager.
 */
public class AchievementSerializationService {
    private static final Logger logger = LoggerFactory.getLogger(AchievementSerializationService.class);
    
    private final AchievementBadgeFormatService badgeFormatService;
    
    public AchievementSerializationService() {
        this.badgeFormatService = new AchievementBadgeFormatService();
    }
    
    /**
     * Serializes achievement list for a user.
     * @param session GameClient session
     * @param achievements Map of all available achievements
     * @return ServerMessage with achievement data
     */
    public ServerMessage serializeAchievementList(GameClient session, Map<Long, Achievement> achievements) {
        if (session == null || session.getHabbo() == null || achievements == null) {
            return new ServerMessage(436); // Empty message
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
            message.appendStringWithBreak(badgeFormatService.formatBadgeCode(
                achievement.getBadgeCode(), 
                level, 
                achievement.isDynamicBadgeLevel()));
        }
        
        return message;
    }
}
