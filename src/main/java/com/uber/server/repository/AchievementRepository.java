package com.uber.server.repository;

import com.uber.server.storage.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for achievement database operations.
 */
public class AchievementRepository {
    private static final Logger logger = LoggerFactory.getLogger(AchievementRepository.class);
    private final DatabasePool databasePool;
    
    public AchievementRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all achievements.
     * @return List of achievement data (id, levels, badge, pixels_base, pixels_multiplier, dynamic_badgelevel)
     */
    public List<Map<String, Object>> loadAchievements() {
        String sql = "SELECT * FROM achievements";
        List<Map<String, Object>> achievements = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> achievement = new HashMap<>();
                achievement.put("id", rs.getLong("id"));
                achievement.put("levels", rs.getInt("levels"));
                achievement.put("badge", rs.getString("badge"));
                achievement.put("pixels_base", rs.getInt("pixels_base"));
                achievement.put("pixels_multiplier", rs.getDouble("pixels_multiplier"));
                achievement.put("dynamic_badgelevel", rs.getString("dynamic_badgelevel"));
                achievements.add(achievement);
            }
        } catch (SQLException e) {
            logger.error("Failed to load achievements: {}", e.getMessage(), e);
        }
        
        return achievements;
    }
    
    /**
     * Loads user achievement progress.
     * @param userId User ID
     * @return Map of achievement ID to level
     */
    public Map<Long, Integer> loadUserAchievements(long userId) {
        String sql = "SELECT achievement_id, achievement_level FROM user_achievements WHERE user_id = ?";
        Map<Long, Integer> achievements = new HashMap<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    achievements.put(rs.getLong("achievement_id"), rs.getInt("achievement_level"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load user achievements for user {}: {}", userId, e.getMessage(), e);
        }
        
        return achievements;
    }
    
    /**
     * Updates or inserts user achievement level.
     * @param userId User ID
     * @param achievementId Achievement ID
     * @param level Achievement level
     * @return True if successful
     */
    public boolean updateUserAchievement(long userId, long achievementId, int level) {
        // First check if achievement exists
        if (hasUserAchievement(userId, achievementId)) {
            return updateUserAchievementLevel(userId, achievementId, level);
        } else {
            return insertUserAchievement(userId, achievementId, level);
        }
    }
    
    /**
     * Updates existing user achievement level.
     * @param userId User ID
     * @param achievementId Achievement ID
     * @param level Achievement level
     * @return True if successful
     */
    public boolean updateUserAchievementLevel(long userId, long achievementId, int level) {
        String sql = """
            UPDATE user_achievements SET achievement_level = ?
            WHERE user_id = ? AND achievement_id = ? LIMIT 1
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, level);
            stmt.setLong(2, userId);
            stmt.setLong(3, achievementId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update user achievement: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Inserts new user achievement.
     * @param userId User ID
     * @param achievementId Achievement ID
     * @param level Achievement level
     * @return True if successful
     */
    public boolean insertUserAchievement(long userId, long achievementId, int level) {
        String sql = """
            INSERT INTO user_achievements (user_id, achievement_id, achievement_level)
            VALUES (?, ?, ?)
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, achievementId);
            stmt.setInt(3, level);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to insert user achievement: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Checks if user has an achievement.
     * @param userId User ID
     * @param achievementId Achievement ID
     * @return True if user has the achievement
     */
    public boolean hasUserAchievement(long userId, long achievementId) {
        String sql = """
            SELECT achievement_level FROM user_achievements
            WHERE user_id = ? AND achievement_id = ? LIMIT 1
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, achievementId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Failed to check user achievement: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets user achievement level.
     * @param userId User ID
     * @param achievementId Achievement ID
     * @return Achievement level, or 0 if not found
     */
    public int getUserAchievementLevel(long userId, long achievementId) {
        String sql = """
            SELECT achievement_level FROM user_achievements
            WHERE user_id = ? AND achievement_id = ? LIMIT 1
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, achievementId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("achievement_level");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user achievement level: {}", e.getMessage(), e);
        }
        
        return 0;
    }
}
