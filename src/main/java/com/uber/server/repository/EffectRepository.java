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
 * Repository for avatar effects database operations.
 */
public class EffectRepository {
    private static final Logger logger = LoggerFactory.getLogger(EffectRepository.class);
    private final DatabasePool databasePool;
    
    public EffectRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all avatar effects for a user.
     * @param userId User ID
     * @return List of effect data (effect_id, total_duration, is_activated, activated_stamp)
     */
    public List<Map<String, Object>> loadEffects(long userId) {
        String sql = "SELECT * FROM user_effects WHERE user_id = ?";
        List<Map<String, Object>> effects = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> effect = new HashMap<>();
                    effect.put("effect_id", rs.getInt("effect_id"));
                    effect.put("total_duration", rs.getInt("total_duration"));
                    effect.put("is_activated", rs.getString("is_activated"));
                    effect.put("activated_stamp", rs.getLong("activated_stamp"));
                    effects.add(effect);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load effects for user {}: {}", userId, e.getMessage(), e);
        }
        
        return effects;
    }
    
    /**
     * Adds a new avatar effect for a user.
     * @param userId User ID
     * @param effectId Effect ID
     * @param duration Total duration
     * @return True if successful
     */
    public boolean addEffect(long userId, int effectId, int duration) {
        String sql = """
            INSERT INTO user_effects (user_id, effect_id, total_duration, is_activated, activated_stamp)
            VALUES (?, ?, ?, '0', 0)
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setInt(2, effectId);
            stmt.setInt(3, duration);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to add effect {} for user {}: {}", effectId, userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes an expired effect from a user.
     * @param userId User ID
     * @param effectId Effect ID
     * @return True if successful
     */
    public boolean deleteEffect(long userId, int effectId) {
        String sql = "DELETE FROM user_effects WHERE user_id = ? AND effect_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setInt(2, effectId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete effect {} for user {}: {}", effectId, userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes an activated effect from a user.
     * @param userId User ID
     * @param effectId Effect ID
     * @return True if successful
     */
    public boolean deleteActivatedEffect(long userId, int effectId) {
        String sql = """
            DELETE FROM user_effects WHERE user_id = ? AND effect_id = ? AND is_activated = '1' LIMIT 1
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setInt(2, effectId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete activated effect {} for user {}: {}", effectId, userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Enables an effect for a user.
     * @param userId User ID
     * @param effectId Effect ID
     * @param timestamp Activation timestamp
     * @return True if successful
     */
    public boolean enableEffect(long userId, int effectId, long timestamp) {
        String sql = """
            UPDATE user_effects SET is_activated = '1', activated_stamp = ?
            WHERE user_id = ? AND effect_id = ? LIMIT 1
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, timestamp);
            stmt.setLong(2, userId);
            stmt.setInt(3, effectId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to enable effect {} for user {}: {}", effectId, userId, e.getMessage(), e);
            return false;
        }
    }
}
