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
 * Repository for room-related database operations.
 */
public class RoomRepository {
    private static final Logger logger = LoggerFactory.getLogger(RoomRepository.class);
    private final DatabasePool databasePool;
    
    public RoomRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Logs a chat message to the database.
     * @param userId User ID
     * @param roomId Room ID
     * @param message Chat message
     * @param userName User name
     * @param timestamp Timestamp
     * @return True if successful
     */
    public boolean logChatMessage(long userId, long roomId, String message, String userName, long timestamp) {
        String sql = "INSERT INTO chatlogs (user_id, room_id, hour, minute, timestamp, message, user_name, full_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            stmt.setLong(1, userId);
            stmt.setLong(2, roomId);
            stmt.setInt(3, now.getHour());
            stmt.setInt(4, now.getMinute());
            stmt.setLong(5, timestamp);
            stmt.setString(6, message);
            stmt.setString(7, userName);
            stmt.setString(8, now.toLocalDate().toString());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to log chat message: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates room icon.
     * @param roomId Room ID
     * @param background Background icon ID
     * @param foreground Foreground icon ID
     * @param items Items string
     * @return True if successful
     */
    public boolean updateRoomIcon(long roomId, int background, int foreground, String items) {
        String sql = "UPDATE rooms SET icon_bg = ?, icon_fg = ?, icon_items = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, background);
            stmt.setInt(2, foreground);
            stmt.setString(3, items);
            stmt.setLong(4, roomId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update room icon: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates room settings.
     * @param roomId Room ID
     * @param caption Room caption
     * @param description Room description
     * @param password Room password
     * @param categoryId Category ID
     * @param state Room state (open/locked/password)
     * @param tags Room tags
     * @param maxUsers Maximum users
     * @param allowPets Allow pets flag
     * @param allowPetsEat Allow pets to eat flag
     * @param allowWalkthrough Allow walkthrough flag
     * @return True if successful
     */
    public boolean updateRoomSettings(long roomId, String caption, String description, String password,
                                      int categoryId, String state, String tags, int maxUsers,
                                      int allowPets, int allowPetsEat, int allowWalkthrough) {
        String sql = "UPDATE rooms SET caption = ?, description = ?, password = ?, category = ?, state = ?, tags = ?, users_max = ?, allow_pets = ?, allow_pets_eat = ?, allow_walkthrough = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, caption);
            stmt.setString(2, description);
            stmt.setString(3, password);
            stmt.setInt(4, categoryId);
            stmt.setString(5, state);
            stmt.setString(6, tags);
            stmt.setInt(7, maxUsers);
            stmt.setInt(8, allowPets);
            stmt.setInt(9, allowPetsEat);
            stmt.setInt(10, allowWalkthrough);
            stmt.setLong(11, roomId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update room settings: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Adds a room right (room manager).
     * @param roomId Room ID
     * @param userId User ID
     * @return True if successful
     */
    public boolean addRoomRight(long roomId, long userId) {
        String sql = "INSERT INTO room_rights (room_id, user_id) VALUES (?, ?)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to add room right: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes room rights.
     * @param roomId Room ID
     * @param userIds List of user IDs to remove (null or empty to remove all)
     * @return True if successful
     */
    public boolean deleteRoomRights(long roomId, long[] userIds) {
        String sql;
        if (userIds == null || userIds.length == 0) {
            sql = "DELETE FROM room_rights WHERE room_id = ?";
        } else {
            sql = "DELETE FROM room_rights WHERE room_id = ? AND user_id IN (";
            for (int i = 0; i < userIds.length; i++) {
                if (i > 0) sql += ",";
                sql += "?";
            }
            sql += ")";
        }
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            if (userIds != null && userIds.length > 0) {
                for (int i = 0; i < userIds.length; i++) {
                    stmt.setLong(i + 2, userIds[i]);
                }
            }
            
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Failed to delete room rights: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes a room and related data.
     * @param roomId Room ID
     * @return True if successful
     */
    public boolean deleteRoom(long roomId) {
        try (Connection conn = databasePool.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Delete favorites
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM user_favorites WHERE room_id = ?")) {
                    stmt.setLong(1, roomId);
                    stmt.executeUpdate();
                }
                
                // Delete room
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM rooms WHERE id = ? LIMIT 1")) {
                    stmt.setLong(1, roomId);
                    stmt.executeUpdate();
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Failed to delete room: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Locks a room.
     * @param roomId Room ID
     * @return True if successful
     */
    public boolean lockRoom(long roomId) {
        String sql = "UPDATE rooms SET state = 'locked' WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to lock room: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Creates a new room.
     * @param caption Room caption/name
     * @param owner Username of owner
     * @param modelName Model name
     * @return The ID of the created room, or 0 if failed
     */
    public long createRoom(String caption, String owner, String modelName) {
        String sql = "INSERT INTO rooms (roomtype, caption, owner, model_name, description, public_ccts, password, tags) " +
                    "VALUES ('private', ?, ?, ?, '', NULL, NULL, '')";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, caption);
            stmt.setString(2, owner);
            stmt.setString(3, modelName);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
                // Fallback: query for the room ID
                return getRoomIdByOwnerAndCaption(owner, caption);
            }
        } catch (SQLException e) {
            logger.error("Failed to create room: {}", e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Gets room ID by owner and caption.
     * @param owner Owner username
     * @param caption Room caption
     * @return Room ID, or 0 if not found
     */
    public long getRoomIdByOwnerAndCaption(String owner, String caption) {
        String sql = "SELECT id FROM rooms WHERE owner = ? AND caption = ? ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, owner);
            stmt.setString(2, caption);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get room ID: {}", e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Gets room data by ID.
     * @param roomId Room ID
     * @return Room data map, or null if not found
     */
    public Map<String, Object> getRoomData(long roomId) {
        String sql = "SELECT * FROM rooms WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractRoomDataFull(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get room data: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Helper method to extract all room data from ResultSet (including icon fields).
     */
    private Map<String, Object> extractRoomDataFull(ResultSet rs) throws SQLException {
        Map<String, Object> room = new HashMap<>();
        room.put("id", rs.getLong("id"));
        room.put("roomtype", rs.getString("roomtype"));
        room.put("caption", rs.getString("caption"));
        room.put("owner", rs.getString("owner"));
        room.put("model_name", rs.getString("model_name"));
        room.put("description", rs.getString("description"));
        room.put("password", rs.getString("password"));
        room.put("tags", rs.getString("tags"));
        room.put("users_now", rs.getInt("users_now"));
        room.put("users_max", rs.getInt("users_max"));
        room.put("state", rs.getString("state"));
        room.put("category", rs.getInt("category"));
        room.put("allow_pets", rs.getInt("allow_pets"));
        room.put("allow_pets_eat", rs.getInt("allow_pets_eat"));
        room.put("allow_walkthrough", rs.getInt("allow_walkthrough"));
        room.put("icon_bg", rs.getInt("icon_bg"));
        room.put("icon_fg", rs.getInt("icon_fg"));
        room.put("icon_items", rs.getString("icon_items"));
        room.put("score", rs.getInt("score"));
        room.put("public_ccts", rs.getString("public_ccts"));
        room.put("wallpaper", rs.getString("wallpaper"));
        room.put("floor", rs.getString("floor"));
        room.put("landscape", rs.getString("landscape"));
        return room;
    }
    
    /**
     * Updates room score.
     * @param roomId Room ID
     * @param score New score
     * @return True if update was successful
     */
    public boolean updateRoomScore(long roomId, int score) {
        String sql = "UPDATE rooms SET score = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, score);
            stmt.setLong(2, roomId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update room score: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates room user count.
     * @param roomId Room ID
     * @param usersNow Current user count
     * @return True if update was successful
     */
    public boolean updateUserCount(long roomId, int usersNow) {
        String sql = "UPDATE rooms SET users_now = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, usersNow);
            stmt.setLong(2, roomId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update room user count: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Resets all rooms' user count to 0.
     * Used during server startup/shutdown cleanup.
     * @return Number of rows updated
     */
    public int resetAllRoomUserCounts() {
        String sql = "UPDATE rooms SET users_now = 0";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to reset room user counts: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Loads room rights (room managers) for a room.
     * @param roomId Room ID
     * @return List of user IDs with room rights
     */
    public List<Long> loadRoomRights(long roomId) {
        String sql = "SELECT user_id FROM room_rights WHERE room_id = ?";
        List<Long> rights = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rights.add(rs.getLong("user_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load room rights for room {}: {}", roomId, e.getMessage(), e);
        }
        
        return rights;
    }
    
    /**
     * Loads all room models from the database.
     * @return List of room model data (id, door_x, door_y, door_z, door_dir, heightmap, public_items, club_only)
     */
    public List<Map<String, Object>> loadRoomModels() {
        String sql = "SELECT id, door_x, door_y, door_z, door_dir, heightmap, public_items, club_only FROM room_models";
        List<Map<String, Object>> models = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> model = new HashMap<>();
                model.put("id", rs.getString("id"));
                model.put("door_x", rs.getInt("door_x"));
                model.put("door_y", rs.getInt("door_y"));
                model.put("door_z", rs.getDouble("door_z"));
                model.put("door_dir", rs.getInt("door_dir"));
                model.put("heightmap", rs.getString("heightmap"));
                model.put("public_items", rs.getString("public_items"));
                model.put("club_only", rs.getString("club_only"));
                models.add(model);
            }
        } catch (SQLException e) {
            logger.error("Failed to load room models: {}", e.getMessage(), e);
        }
        
        return models;
    }
    
    /**
     * Updates room decoration (wallpaper, floor, or landscape).
     * @param roomId Room ID
     * @param type Decoration type (wallpaper, floor, or landscape)
     * @param value Decoration value
     * @return True if update was successful
     */
    public boolean updateRoomDecoration(long roomId, String type, String value) {
        String sql = "UPDATE rooms SET " + type + " = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, value);
            stmt.setLong(2, roomId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update room decoration {} for room {}: {}", type, roomId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates room state (open, locked, password).
     * @param roomId Room ID
     * @param state Room state (0 = open, 1 = locked, 2 = password)
     * @return True if update was successful
     */
    public boolean updateRoomState(long roomId, int state) {
        String stateStr = "open";
        if (state == 1) {
            stateStr = "locked";
        } else if (state == 2) {
            stateStr = "password";
        }
        
        String sql = "UPDATE rooms SET state = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, stateStr);
            stmt.setLong(2, roomId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update room state for room {}: {}", roomId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Marks room as inappropriate.
     * @param roomId Room ID
     * @return True if update was successful
     */
    public boolean updateRoomInappropriate(long roomId) {
        String sql = "UPDATE rooms SET caption = 'Inappropriate to Hotel Management', " +
                    "description = 'Inappropriate to Hotel Management', tags = '' WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update room as inappropriate for room {}: {}", roomId, e.getMessage(), e);
            return false;
        }
    }
}
