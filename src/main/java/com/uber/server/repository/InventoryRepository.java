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
 * Repository for inventory database operations.
 */
public class InventoryRepository {
    private static final Logger logger = LoggerFactory.getLogger(InventoryRepository.class);
    private final DatabasePool databasePool;
    
    public InventoryRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads user inventory items.
     * @param userId User ID
     * @return List of inventory item data (id, base_item, extra_data)
     */
    public List<Map<String, Object>> loadUserItems(long userId) {
        String sql = "SELECT id, base_item, extra_data FROM user_items WHERE user_id = ?";
        List<Map<String, Object>> items = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getLong("id"));
                    item.put("base_item", rs.getLong("base_item"));
                    item.put("extra_data", rs.getString("extra_data"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load user items for user {}: {}", userId, e.getMessage(), e);
        }
        
        return items;
    }
    
    /**
     * Creates a new user item.
     * @param itemId Item ID
     * @param userId User ID
     * @param baseItem Base item ID
     * @param extraData Extra data
     * @return True if creation was successful
     */
    public boolean createUserItem(long itemId, long userId, long baseItem, String extraData) {
        String sql = "INSERT INTO user_items (id, user_id, base_item, extra_data) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            stmt.setLong(2, userId);
            stmt.setLong(3, baseItem);
            stmt.setString(4, extraData);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to create user item: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Creates a user present (gift).
     * @param itemId Item ID
     * @param baseId Base item ID
     * @param amount Amount
     * @param extraData Extra data
     * @return True if creation was successful
     */
    public boolean createUserPresent(long itemId, long baseId, int amount, String extraData) {
        String sql = "INSERT INTO user_presents (item_id, base_id, amount, extra_data) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            stmt.setLong(2, baseId);
            stmt.setInt(3, amount);
            stmt.setString(4, extraData);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to create user present: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Loads user pets in inventory (not placed in room).
     * @param userId User ID
     * @return List of pet data
     */
    public List<Map<String, Object>> loadUserPets(long userId) {
        String sql = "SELECT * FROM user_pets WHERE user_id = ? AND room_id <= 0";
        List<Map<String, Object>> pets = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> pet = new HashMap<>();
                    pet.put("id", rs.getLong("id"));
                    pet.put("user_id", rs.getLong("user_id"));
                    pet.put("name", rs.getString("name"));
                    pet.put("type", rs.getInt("type"));
                    pet.put("race", rs.getString("race"));
                    pet.put("color", rs.getString("color"));
                    pet.put("expirience", rs.getInt("expirience"));
                    pet.put("energy", rs.getInt("energy"));
                    pet.put("nutrition", rs.getInt("nutrition"));
                    pet.put("respect", rs.getInt("respect"));
                    pet.put("createstamp", rs.getDouble("createstamp"));
                    pet.put("room_id", rs.getLong("room_id"));
                    pet.put("x", rs.getInt("x"));
                    pet.put("y", rs.getInt("y"));
                    pet.put("z", rs.getDouble("z"));
                    pets.add(pet);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load user pets for user {}: {}", userId, e.getMessage(), e);
        }
        
        return pets;
    }
    
    /**
     * Loads user avatar effects.
     * @param userId User ID
     * @return List of effect data (effect_id, total_duration, is_activated, activated_stamp)
     */
    public List<Map<String, Object>> loadUserEffects(long userId) {
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
                    effect.put("is_activated", rs.getInt("is_activated"));
                    effect.put("activated_stamp", rs.getLong("activated_stamp"));
                    effects.add(effect);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load user effects for user {}: {}", userId, e.getMessage(), e);
        }
        
        return effects;
    }
    
    /**
     * Creates a new user avatar effect.
     * @param userId User ID
     * @param effectId Effect ID
     * @param duration Total duration
     * @return True if creation was successful
     */
    public boolean createUserEffect(long userId, int effectId, int duration) {
        String sql = """
            INSERT INTO user_effects (user_id, effect_id, total_duration, is_activated, activated_stamp)
            VALUES (?, ?, ?, '0', '0')
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setInt(2, effectId);
            stmt.setInt(3, duration);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to create user effect: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes an item from user inventory.
     * @param userId User ID
     * @param itemId Item ID
     * @return True if deletion was successful
     */
    public boolean deleteItem(long userId, long itemId) {
        String sql = "DELETE FROM user_items WHERE id = ? AND user_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete item {} for user {}: {}", itemId, userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes all items for a user.
     * @param userId User ID
     * @return True if deletion was successful
     */
    public boolean deleteAllItems(long userId) {
        String sql = "DELETE FROM user_items WHERE user_id = ?";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Failed to delete all items for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets user present data for an item.
     * @param itemId Item ID
     * @return Present data as Map (base_id, amount, extra_data), or null if not found
     */
    public Map<String, Object> getUserPresent(long itemId) {
        String sql = "SELECT base_id, amount, extra_data FROM user_presents WHERE item_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> present = new HashMap<>();
                    present.put("base_id", rs.getLong("base_id"));
                    present.put("amount", rs.getInt("amount"));
                    present.put("extra_data", rs.getString("extra_data"));
                    return present;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user present for item {}: {}", itemId, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Deletes a user present.
     * @param itemId Item ID
     * @return True if deletion was successful
     */
    public boolean deleteUserPresent(long itemId) {
        String sql = "DELETE FROM user_presents WHERE item_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete user present for item {}: {}", itemId, e.getMessage(), e);
            return false;
        }
    }
}
