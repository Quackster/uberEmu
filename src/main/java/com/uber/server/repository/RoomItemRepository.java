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
 * Repository for room item database operations.
 */
public class RoomItemRepository {
    private static final Logger logger = LoggerFactory.getLogger(RoomItemRepository.class);
    private final DatabasePool databasePool;
    
    public RoomItemRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Updates room item extra_data.
     * @param itemId Item ID
     * @param extraData Extra data string
     * @return True if update was successful
     */
    public boolean updateExtraData(long itemId, String extraData) {
        String sql = "UPDATE room_items SET extra_data = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, extraData);
            stmt.setLong(2, itemId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update room item extra_data for item {}: {}", itemId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates room item position and rotation.
     * @param itemId Item ID
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param rot Rotation
     * @return True if update was successful
     */
    public boolean updatePosition(long itemId, int x, int y, double z, int rot) {
        String sql = "UPDATE room_items SET x = ?, y = ?, z = ?, rot = ?, wall_pos = '' WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setDouble(3, z);
            stmt.setInt(4, rot);
            stmt.setLong(5, itemId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update room item position for item {}: {}", itemId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Creates a new floor item in a room.
     * @param itemId Item ID
     * @param roomId Room ID
     * @param baseItem Base item ID
     * @param extraData Extra data
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param rot Rotation
     * @return True if creation was successful
     */
    public boolean createFloorItem(long itemId, long roomId, long baseItem, String extraData, 
                                   int x, int y, double z, int rot) {
        String sql = "INSERT INTO room_items (id, room_id, base_item, extra_data, x, y, z, rot, wall_pos) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, '')";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            stmt.setLong(2, roomId);
            stmt.setLong(3, baseItem);
            stmt.setString(4, extraData);
            stmt.setInt(5, x);
            stmt.setInt(6, y);
            stmt.setDouble(7, z);
            stmt.setInt(8, rot);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to create floor item: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Creates a new wall item in a room.
     * @param itemId Item ID
     * @param roomId Room ID
     * @param baseItem Base item ID
     * @param extraData Extra data
     * @param wallPos Wall position string
     * @return True if creation was successful
     */
    public boolean createWallItem(long itemId, long roomId, long baseItem, String extraData, String wallPos) {
        String sql = "INSERT INTO room_items (id, room_id, base_item, extra_data, x, y, z, rot, wall_pos) " +
                    "VALUES (?, ?, ?, ?, 0, 0, 0, 0, ?)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            stmt.setLong(2, roomId);
            stmt.setLong(3, baseItem);
            stmt.setString(4, extraData);
            stmt.setString(5, wallPos);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to create wall item: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Loads all room items for a room.
     * @param roomId Room ID
     * @return List of room item data
     */
    public List<Map<String, Object>> loadRoomItems(long roomId) {
        String sql = "SELECT * FROM room_items WHERE room_id = ?";
        List<Map<String, Object>> items = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getLong("id"));
                    item.put("room_id", rs.getLong("room_id"));
                    item.put("base_item", rs.getLong("base_item"));
                    item.put("extra_data", rs.getString("extra_data"));
                    item.put("x", rs.getInt("x"));
                    item.put("y", rs.getInt("y"));
                    item.put("z", rs.getDouble("z"));
                    item.put("rot", rs.getInt("rot"));
                    item.put("wall_pos", rs.getString("wall_pos"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load room items for room {}: {}", roomId, e.getMessage(), e);
        }
        
        return items;
    }
    
    /**
     * Gets the room ID for a specific item.
     * @param itemId Item ID
     * @return Room ID, or 0 if not found
     */
    public long getRoomIdForItem(long itemId) {
        String sql = "SELECT room_id FROM room_items WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("room_id");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get room ID for item {}: {}", itemId, e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Creates a new room item (generic - handles both floor and wall items).
     * @param itemId Item ID
     * @param roomId Room ID
     * @param baseItem Base item ID
     * @param extraData Extra data
     * @param x X coordinate (0 for wall items)
     * @param y Y coordinate (0 for wall items)
     * @param z Z coordinate (0 for wall items)
     * @param rot Rotation (0 for wall items)
     * @param wallPos Wall position string (empty for floor items)
     * @return True if creation was successful
     */
    public boolean createRoomItem(long itemId, long roomId, long baseItem, String extraData,
                                  int x, int y, double z, int rot, String wallPos) {
        String sql = "INSERT INTO room_items (id, room_id, base_item, extra_data, x, y, z, rot, wall_pos) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            stmt.setLong(2, roomId);
            stmt.setLong(3, baseItem);
            stmt.setString(4, extraData != null ? extraData : "");
            stmt.setInt(5, x);
            stmt.setInt(6, y);
            stmt.setDouble(7, z);
            stmt.setInt(8, rot);
            stmt.setString(9, wallPos != null ? wallPos : "");
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to create room item: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes a room item from the database.
     * @param itemId Item ID
     * @return True if deletion was successful
     */
    public boolean deleteRoomItem(long itemId) {
        String sql = "DELETE FROM room_items WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete room item {}: {}", itemId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes all room items for a room.
     * @param roomId Room ID
     * @return True if deletion was successful
     */
    public boolean deleteRoomItems(long roomId) {
        String sql = "DELETE FROM room_items WHERE room_id = ?";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Failed to delete room items for room {}: {}", roomId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Loads moodlight data for an item.
     * @param itemId Item ID
     * @return Moodlight data as Map, or null if not found
     */
    public Map<String, Object> loadMoodlightData(long itemId) {
        String sql = "SELECT enabled, current_preset, preset_one, preset_two, preset_three " +
                    "FROM room_items_moodlight WHERE item_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("enabled", rs.getString("enabled"));
                    data.put("current_preset", rs.getInt("current_preset"));
                    data.put("preset_one", rs.getString("preset_one"));
                    data.put("preset_two", rs.getString("preset_two"));
                    data.put("preset_three", rs.getString("preset_three"));
                    return data;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load moodlight data for item {}: {}", itemId, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Updates moodlight enabled status.
     * @param itemId Item ID
     * @param enabled Enabled status
     * @return True if update was successful
     */
    public boolean updateMoodlightEnabled(long itemId, boolean enabled) {
        String sql = "UPDATE room_items_moodlight SET enabled = ? WHERE item_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, enabled ? "1" : "0");
            stmt.setLong(2, itemId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update moodlight enabled status for item {}: {}", itemId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates a moodlight preset.
     * @param itemId Item ID
     * @param presetField Preset field name (preset_one, preset_two, preset_three)
     * @param presetValue Preset value string
     * @return True if update was successful
     */
    public boolean updateMoodlightPreset(long itemId, String presetField, String presetValue) {
        String sql = "UPDATE room_items_moodlight SET " + presetField + " = ? WHERE item_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, presetValue);
            stmt.setLong(2, itemId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update moodlight preset for item {}: {}", itemId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets the linked teleport ID for a teleport.
     * @param teleId Teleport item ID
     * @return Linked teleport ID, or null if not found
     */
    public Long getLinkedTele(long teleId) {
        String sql = "SELECT tele_two_id FROM tele_links WHERE tele_one_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, teleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("tele_two_id");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get linked tele for {}: {}", teleId, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Gets the room ID for a teleport item.
     * @param teleId Teleport item ID
     * @return Room ID, or null if not found
     */
    public Long getTeleRoomId(long teleId) {
        String sql = "SELECT room_id FROM room_items WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, teleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("room_id");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get tele room ID for {}: {}", teleId, e.getMessage(), e);
        }
        
        return null;
    }
}
