package com.uber.server.repository;

import com.uber.server.storage.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository for badge-related database operations.
 */
public class BadgeRepository {
    private static final Logger logger = LoggerFactory.getLogger(BadgeRepository.class);
    private final DatabasePool databasePool;
    
    public BadgeRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all badges for a user.
     * @param userId User ID
     * @return List of badge data maps (badge_id, badge_slot)
     */
    public List<Map<String, Object>> loadBadges(long userId) {
        List<Map<String, Object>> badges = new ArrayList<>();
        String sql = "SELECT badge_id, badge_slot FROM user_badges WHERE user_id = ?";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> badge = new java.util.HashMap<>();
                    badge.put("badge_id", rs.getString("badge_id"));
                    badge.put("badge_slot", rs.getInt("badge_slot"));
                    badges.add(badge);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load badges for user {}: {}", userId, e.getMessage(), e);
        }
        
        return badges;
    }
    
    /**
     * Adds a badge to a user.
     * @param userId User ID
     * @param badgeId Badge ID
     * @param slot Badge slot (0 = unequipped)
     * @return True if successful
     */
    public boolean addBadge(long userId, String badgeId, int slot) {
        String sql = "INSERT INTO user_badges (user_id, badge_id, badge_slot) VALUES (?, ?, ?)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setString(2, badgeId);
            stmt.setInt(3, slot);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to add badge {} to user {}: {}", badgeId, userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Removes a badge from a user.
     * @param userId User ID
     * @param badgeId Badge ID
     * @return True if successful
     */
    public boolean removeBadge(long userId, String badgeId) {
        String sql = "DELETE FROM user_badges WHERE badge_id = ? AND user_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, badgeId);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to remove badge {} from user {}: {}", badgeId, userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Resets all badge slots for a user (sets all to 0).
     * @param userId User ID
     * @return True if successful
     */
    public boolean resetBadgeSlots(long userId) {
        String sql = "UPDATE user_badges SET badge_slot = 0 WHERE user_id = ?";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Failed to reset badge slots for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates a badge slot.
     * @param userId User ID
     * @param badgeId Badge ID
     * @param slot New slot number
     * @return True if successful
     */
    public boolean updateBadgeSlot(long userId, String badgeId, int slot) {
        String sql = "UPDATE user_badges SET badge_slot = ? WHERE badge_id = ? AND user_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, slot);
            stmt.setString(2, badgeId);
            stmt.setLong(3, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update badge slot for user {} badge {}: {}", userId, badgeId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gives a badge to a user (adds if not exists, updates slot if exists).
     * @param userId User ID
     * @param badgeId Badge ID
     * @param slot Badge slot (0 = unequipped)
     * @param insertIfNotExists If true, inserts badge if it doesn't exist
     * @return True if successful
     */
    public boolean giveBadge(long userId, String badgeId, int slot, boolean insertIfNotExists) {
        // First check if badge exists
        String checkSql = "SELECT badge_id FROM user_badges WHERE user_id = ? AND badge_id = ? LIMIT 1";

        try (Connection conn = databasePool.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setLong(1, userId);
            checkStmt.setString(2, badgeId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // Badge exists, update slot if needed
                    if (slot != 0) {
                        return updateBadgeSlot(userId, badgeId, slot);
                    }
                    return true;
                } else if (insertIfNotExists) {
                    // Badge doesn't exist and we should insert it
                    return addBadge(userId, badgeId, slot);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to give badge {} to user {}: {}", badgeId, userId, e.getMessage(), e);
            return false;
        }

        return false;
    }
}
