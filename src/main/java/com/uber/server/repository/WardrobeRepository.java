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
 * Repository for wardrobe-related database operations.
 */
public class WardrobeRepository {
    private static final Logger logger = LoggerFactory.getLogger(WardrobeRepository.class);
    private final DatabasePool databasePool;
    
    public WardrobeRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all wardrobe items for a user.
     * @param userId User ID
     * @return List of wardrobe item data maps (slot_id, look, gender)
     */
    public List<Map<String, Object>> loadWardrobe(long userId) {
        List<Map<String, Object>> wardrobe = new ArrayList<>();
        String sql = "SELECT * FROM user_wardrobe WHERE user_id = ?";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("slot_id", rs.getLong("slot_id"));
                    item.put("look", rs.getString("look"));
                    item.put("gender", rs.getString("gender"));
                    wardrobe.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load wardrobe for user {}: {}", userId, e.getMessage(), e);
        }
        
        return wardrobe;
    }
    
    /**
     * Saves or updates a wardrobe item.
     * @param userId User ID
     * @param slotId Slot ID
     * @param look Look string
     * @param gender Gender (M/F)
     * @return True if successful
     */
    public boolean saveWardrobeItem(long userId, long slotId, String look, String gender) {
        // Check if item exists
        if (wardrobeItemExists(userId, slotId)) {
            return updateWardrobeItem(userId, slotId, look, gender);
        } else {
            return insertWardrobeItem(userId, slotId, look, gender);
        }
    }
    
    /**
     * Checks if a wardrobe item exists.
     * @param userId User ID
     * @param slotId Slot ID
     * @return True if exists
     */
    private boolean wardrobeItemExists(long userId, long slotId) {
        String sql = "SELECT NULL FROM user_wardrobe WHERE user_id = ? AND slot_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, slotId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Failed to check wardrobe item existence: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates an existing wardrobe item.
     * @param userId User ID
     * @param slotId Slot ID
     * @param look Look string
     * @param gender Gender (M/F)
     * @return True if successful
     */
    private boolean updateWardrobeItem(long userId, long slotId, String look, String gender) {
        String sql = "UPDATE user_wardrobe SET look = ?, gender = ? WHERE user_id = ? AND slot_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, look);
            stmt.setString(2, gender);
            stmt.setLong(3, userId);
            stmt.setLong(4, slotId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update wardrobe item: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Inserts a new wardrobe item.
     * @param userId User ID
     * @param slotId Slot ID
     * @param look Look string
     * @param gender Gender (M/F)
     * @return True if successful
     */
    private boolean insertWardrobeItem(long userId, long slotId, String look, String gender) {
        String sql = "INSERT INTO user_wardrobe (user_id, slot_id, look, gender) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, slotId);
            stmt.setString(3, look);
            stmt.setString(4, gender);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to insert wardrobe item: {}", e.getMessage(), e);
            return false;
        }
    }
}
