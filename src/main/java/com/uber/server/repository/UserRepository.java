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
 * Repository for user-related database operations.
 */
public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private final DatabasePool databasePool;
    
    public UserRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Authenticates a user by auth ticket.
     * @param authTicket The authentication ticket
     * @return User data as Map, or null if not found
     */
    public Map<String, Object> authenticateUser(String authTicket) {
        String sql = """
            SELECT * FROM users
            WHERE auth_ticket = ?
            LIMIT 1
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, authTicket);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", rs.getLong("id"));
                    userData.put("username", rs.getString("username"));
                    userData.put("real_name", rs.getString("real_name"));
                    userData.put("rank", rs.getLong("rank"));
                    userData.put("motto", rs.getString("motto"));
                    userData.put("look", rs.getString("look"));
                    userData.put("gender", rs.getString("gender"));
                    userData.put("credits", rs.getInt("credits"));
                    userData.put("activity_points", rs.getInt("activity_points"));
                    userData.put("activity_points_lastupdate", rs.getLong("activity_points_lastupdate"));
                    userData.put("is_muted", rs.getInt("is_muted"));
                    userData.put("home_room", rs.getLong("home_room"));
                    userData.put("respect", rs.getInt("respect"));
                    userData.put("daily_respect_points", rs.getInt("daily_respect_points"));
                    userData.put("daily_pet_respect_points", rs.getInt("daily_pet_respect_points"));
                    userData.put("newbie_status", rs.getInt("newbie_status"));
                    userData.put("mutant_penalty", rs.getInt("mutant_penalty"));
                    userData.put("block_newfriends", rs.getString("block_newfriends"));
                    return userData;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to authenticate user: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Updates user look and gender.
     * @param userId User ID
     * @param look New look string
     * @param gender New gender (M/F)
     * @return True if update was successful
     */
    public boolean updateLook(long userId, String look, String gender) {
        String sql = """
            UPDATE users
            SET look = ?, gender = ?
            WHERE id = ?
            LIMIT 1
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, look);
            stmt.setString(2, gender);
            stmt.setLong(3, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update user look: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates user online status and auth ticket.
     * @param userId User ID
     * @param online Online status (1 = online, 0 = offline)
     * @param authTicket Auth ticket (empty string to clear)
     * @param ipAddress Last IP address
     * @return True if update was successful
     */
    public boolean updateOnlineStatus(long userId, int online, String authTicket, String ipAddress) {
        String sql = "UPDATE users SET online = ?, auth_ticket = ?, ip_last = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, online);
            stmt.setString(2, authTicket);
            stmt.setString(3, ipAddress);
            stmt.setLong(4, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update user online status: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates user login timestamp.
     * @param userId User ID
     * @param timestamp Login timestamp
     * @return True if update was successful
     */
    public boolean updateLoginTimestamp(long userId, long timestamp) {
        String sql = "UPDATE user_info SET login_timestamp = ? WHERE user_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, timestamp);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update login timestamp: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates user home room.
     * @param userId User ID
     * @param roomId Home room ID
     * @return True if update was successful
     */
    /**
     * Updates home room for all users who have a specific room as home room.
     * @param oldRoomId Old room ID
     * @param newRoomId New room ID (usually 0)
     * @return Number of rows updated
     */
    public int updateHomeRoomForRoom(long oldRoomId, long newRoomId) {
        String sql = "UPDATE users SET home_room = ? WHERE home_room = ?";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, newRoomId);
            stmt.setLong(2, oldRoomId);
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update home room for room {}: {}", oldRoomId, e.getMessage(), e);
            return 0;
        }
    }
    
    public boolean updateHomeRoom(long userId, long roomId) {
        String sql = "UPDATE users SET home_room = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update home room: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets user credits.
     * @param userId User ID
     * @return Credits amount, or 0 if not found
     */
    public int getCredits(long userId) {
        String sql = "SELECT credits FROM users WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("credits");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user credits: {}", e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Updates user credits.
     * @param userId User ID
     * @param credits New credits amount
     * @return True if update was successful
     */
    public boolean updateCredits(long userId, int credits) {
        String sql = "UPDATE users SET credits = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, credits);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update user credits: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates user muted status.
     * @param userId User ID
     * @param muted Muted status (true = muted, false = not muted)
     * @return True if update was successful
     */
    public boolean updateMutedStatus(long userId, boolean muted) {
        String sql = "UPDATE users SET is_muted = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, muted ? 1 : 0);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update muted status for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates server status for all users (clears auth tickets and sets online status).
     * Used during server startup/shutdown cleanup.
     * @param onlineStatus Online status to set (1 = online, 0 = offline)
     * @return Number of rows updated
     */
    public int updateServerStatus(int onlineStatus) {
        String sql = "UPDATE users SET online = ?";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, onlineStatus);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update server status: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Logs a room visit entry.
     * @param userId User ID
     * @param roomId Room ID
     * @param timestamp Entry timestamp (Unix timestamp)
     * @param hour Hour (0-23)
     * @param minute Minute (0-59)
     * @return True if successful
     */
    public boolean logRoomVisit(long userId, long roomId, long timestamp, int hour, int minute) {
        String sql = "INSERT INTO user_roomvisits (user_id, room_id, entry_timestamp, hour, minute, exit_timestamp) VALUES (?, ?, ?, ?, ?, 0)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, roomId);
            stmt.setLong(3, timestamp);
            stmt.setInt(4, hour);
            stmt.setInt(5, minute);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to log room visit: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates room visit exit timestamp for a specific user and room.
     * @param userId User ID
     * @param roomId Room ID
     * @param timestamp Exit timestamp (Unix timestamp)
     * @return True if successful
     */
    public boolean updateRoomVisitExit(long userId, long roomId, long timestamp) {
        String sql = "UPDATE user_roomvisits SET exit_timestamp = ? WHERE user_id = ? AND room_id = ? AND exit_timestamp <= 0 ORDER BY entry_timestamp DESC LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, timestamp);
            stmt.setLong(2, userId);
            stmt.setLong(3, roomId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update room visit exit: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates user room visits exit timestamp for users still in rooms.
     * @param exitTimestamp Exit timestamp (Unix timestamp)
     * @return Number of rows updated
     */
    public int updateRoomVisitExits(long exitTimestamp) {
        String sql = "UPDATE user_roomvisits SET exit_timestamp = ? WHERE exit_timestamp <= 0";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, exitTimestamp);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update room visit exits: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Loads favorite rooms for a user.
     * @param userId User ID
     * @return List of favorite room IDs
     */
    public List<Long> loadFavorites(long userId) {
        String sql = "SELECT room_id FROM user_favorites WHERE user_id = ?";
        List<Long> favorites = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    favorites.add(rs.getLong("room_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load favorites for user {}: {}", userId, e.getMessage(), e);
        }
        
        return favorites;
    }
    
    /**
     * Adds a favorite room for a user.
     * @param userId User ID
     * @param roomId Room ID
     * @return True if successful
     */
    public boolean addFavorite(long userId, long roomId) {
        String sql = "INSERT INTO user_favorites (user_id, room_id) VALUES (?, ?)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, roomId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to add favorite room for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Removes a favorite room for a user.
     * @param userId User ID
     * @param roomId Room ID
     * @return True if successful
     */
    public boolean removeFavorite(long userId, long roomId) {
        String sql = "DELETE FROM user_favorites WHERE user_id = ? AND room_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, roomId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to remove favorite room for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Loads muted users for a user.
     * @param userId User ID
     * @return List of muted user IDs
     */
    public List<Long> loadMutedUsers(long userId) {
        String sql = "SELECT ignore_id FROM user_ignores WHERE user_id = ?";
        List<Long> muted = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    muted.add(rs.getLong("ignore_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load muted users for user {}: {}", userId, e.getMessage(), e);
        }
        
        return muted;
    }
    
    /**
     * Loads user tags.
     * @param userId User ID
     * @return List of tag strings
     */
    public List<String> loadTags(long userId) {
        String sql = "SELECT tag FROM user_tags WHERE user_id = ?";
        List<String> tags = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(rs.getString("tag"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load tags for user {}: {}", userId, e.getMessage(), e);
        }
        
        return tags;
    }
    
    /**
     * Updates user last online timestamp and online status.
     * @param userId User ID
     * @param online Online status (1 = online, 0 = offline)
     * @return True if update was successful
     */
    public boolean updateLastOnline(long userId, int online) {
        String sql = "UPDATE users SET last_online = NOW(), online = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, online);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update last online for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets activity points for a user.
     * @param userId User ID
     * @return Activity points, or 0 if not found
     */
    public int getActivityPoints(long userId) {
        String sql = "SELECT activity_points FROM users WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("activity_points");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get activity points for user {}: {}", userId, e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Updates activity points for a user.
     * @param userId User ID
     * @param activityPoints New activity points amount
     * @param lastUpdate Last update timestamp
     * @return True if update was successful
     */
    public boolean updateActivityPoints(long userId, int activityPoints, long lastUpdate) {
        String sql = "UPDATE users SET activity_points = ?, activity_points_lastupdate = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, activityPoints);
            stmt.setLong(2, lastUpdate);
            stmt.setLong(3, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update activity points for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets user data by user ID.
     * @param userId User ID
     * @return User data as Map, or null if not found
     */
    public Map<String, Object> getUser(long userId) {
        String sql = "SELECT * FROM users WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", rs.getLong("id"));
                    userData.put("username", rs.getString("username"));
                    userData.put("real_name", rs.getString("real_name"));
                    userData.put("rank", rs.getLong("rank"));
                    userData.put("motto", rs.getString("motto"));
                    userData.put("look", rs.getString("look"));
                    userData.put("gender", rs.getString("gender"));
                    userData.put("credits", rs.getInt("credits"));
                    userData.put("activity_points", rs.getInt("activity_points"));
                    userData.put("activity_points_lastupdate", rs.getLong("activity_points_lastupdate"));
                    userData.put("is_muted", rs.getInt("is_muted"));
                    userData.put("home_room", rs.getLong("home_room"));
                    userData.put("respect", rs.getInt("respect"));
                    userData.put("daily_respect_points", rs.getInt("daily_respect_points"));
                    userData.put("daily_pet_respect_points", rs.getInt("daily_pet_respect_points"));
                    userData.put("newbie_status", rs.getInt("newbie_status"));
                    userData.put("mutant_penalty", rs.getInt("mutant_penalty"));
                    userData.put("block_newfriends", rs.getString("block_newfriends"));
                    userData.put("online", rs.getInt("online"));
                    return userData;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user {}: {}", userId, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Gets user ID by username.
     * @param username Username
     * @return User ID, or 0 if not found
     */
    public long getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user ID by username {}: {}", username, e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Gets real name for a user.
     * @param userId User ID
     * @return Real name, or null if not found
     */
    public String getRealName(long userId) {
        String sql = "SELECT real_name FROM users WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("real_name");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get real name for user {}: {}", userId, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Gets last online timestamp for a user.
     * @param userId User ID
     * @return Last online timestamp, or 0 if not found
     */
    public long getLastOnline(long userId) {
        String sql = "SELECT last_online FROM users WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("last_online");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get last online for user {}: {}", userId, e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Updates user's respect count.
     * @param userId User ID
     * @param respect New respect count
     * @return True if successful
     */
    public boolean updateRespect(long userId, int respect) {
        String sql = "UPDATE users SET respect = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, respect);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update respect for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates user's daily respect points.
     * @param userId User ID
     * @param dailyRespectPoints New daily respect points count
     * @return True if successful
     */
    public boolean updateDailyRespectPoints(long userId, int dailyRespectPoints) {
        String sql = "UPDATE users SET daily_respect_points = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, dailyRespectPoints);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update daily respect points for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates daily pet respect points for a user.
     * @param userId User ID
     * @param dailyPetRespectPoints New daily pet respect points value
     * @return True if update was successful
     */
    public boolean updateDailyPetRespectPoints(long userId, int dailyPetRespectPoints) {
        String sql = "UPDATE users SET daily_pet_respect_points = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, dailyPetRespectPoints);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update daily pet respect points for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates newbie status for a user.
     * @param userId User ID
     * @param newbieStatus New newbie status (0, 1, or 2)
     * @return True if update was successful
     */
    public boolean updateNewbieStatus(long userId, int newbieStatus) {
        String sql = "UPDATE users SET newbie_status = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, newbieStatus);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update newbie status for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
}
