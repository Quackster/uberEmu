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
 * Repository for messenger database operations.
 */
public class MessengerRepository {
    private static final Logger logger = LoggerFactory.getLogger(MessengerRepository.class);
    private final DatabasePool databasePool;
    
    public MessengerRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all buddies (friends) for a user.
     * @param userId User ID
     * @return List of buddy user IDs (user_two_id)
     */
    public List<Long> loadBuddies(long userId) {
        String sql = "SELECT user_two_id FROM messenger_friendships WHERE user_one_id = ?";
        List<Long> buddies = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    buddies.add(rs.getLong("user_two_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load buddies for user {}: {}", userId, e.getMessage(), e);
        }
        
        return buddies;
    }
    
    /**
     * Loads all friend requests for a user.
     * @param userId User ID (to_id)
     * @return List of request data (id, to_id, from_id)
     */
    public List<Map<String, Object>> loadRequests(long userId) {
        String sql = "SELECT * FROM messenger_requests WHERE to_id = ?";
        List<Map<String, Object>> requests = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> request = new HashMap<>();
                    request.put("id", rs.getLong("id"));
                    request.put("to_id", rs.getLong("to_id"));
                    request.put("from_id", rs.getLong("from_id"));
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load requests for user {}: {}", userId, e.getMessage(), e);
        }
        
        return requests;
    }
    
    /**
     * Checks if a friendship request exists between two users.
     * @param toId To user ID
     * @param fromId From user ID
     * @return True if request exists
     */
    public boolean requestExists(long toId, long fromId) {
        String sql = "SELECT * FROM messenger_requests WHERE to_id = ? AND from_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, toId);
            stmt.setLong(2, fromId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Failed to check request existence: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Checks if a friendship exists between two users.
     * @param userOne First user ID
     * @param userTwo Second user ID
     * @return True if friendship exists
     */
    public boolean friendshipExists(long userOne, long userTwo) {
        String sql = """
            SELECT * FROM messenger_friendships WHERE (user_one_id = ? AND user_two_id = ?)
            OR (user_one_id = ? AND user_two_id = ?) LIMIT 1
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userOne);
            stmt.setLong(2, userTwo);
            stmt.setLong(3, userTwo);
            stmt.setLong(4, userOne);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Failed to check friendship existence: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes all requests for a user.
     * @param userId User ID
     * @return True if successful
     */
    public boolean deleteAllRequests(long userId) {
        String sql = "DELETE FROM messenger_requests WHERE to_id = ?";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Failed to delete all requests for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes a specific request.
     * @param toId To user ID
     * @param fromId From user ID
     * @return True if successful
     */
    public boolean deleteRequest(long toId, long fromId) {
        String sql = "DELETE FROM messenger_requests WHERE to_id = ? AND from_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, toId);
            stmt.setLong(2, fromId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete request: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Creates a friendship between two users (bidirectional).
     * @param userOne First user ID
     * @param userTwo Second user ID
     * @return True if successful
     */
    public boolean createFriendship(long userOne, long userTwo) {
        String sql1 = "INSERT INTO messenger_friendships (user_one_id, user_two_id) VALUES (?, ?)";
        String sql2 = "INSERT INTO messenger_friendships (user_one_id, user_two_id) VALUES (?, ?)";
        
        try (Connection conn = databasePool.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt1 = conn.prepareStatement(sql1);
                 PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                
                stmt1.setLong(1, userOne);
                stmt1.setLong(2, userTwo);
                stmt1.executeUpdate();
                
                stmt2.setLong(1, userTwo);
                stmt2.setLong(2, userOne);
                stmt2.executeUpdate();
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Failed to create friendship between {} and {}: {}", userOne, userTwo, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes a friendship between two users (bidirectional).
     * @param userOne First user ID
     * @param userTwo Second user ID
     * @return True if successful
     */
    public boolean deleteFriendship(long userOne, long userTwo) {
        String sql1 = "DELETE FROM messenger_friendships WHERE user_one_id = ? AND user_two_id = ? LIMIT 1";
        String sql2 = "DELETE FROM messenger_friendships WHERE user_one_id = ? AND user_two_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt1 = conn.prepareStatement(sql1);
                 PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                
                stmt1.setLong(1, userOne);
                stmt1.setLong(2, userTwo);
                stmt1.executeUpdate();
                
                stmt2.setLong(1, userTwo);
                stmt2.setLong(2, userOne);
                stmt2.executeUpdate();
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Failed to delete friendship between {} and {}: {}", userOne, userTwo, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Creates a new friend request.
     * @param toId To user ID
     * @param fromId From user ID
     * @return The ID of the created request, or 0 if failed
     */
    public long createRequest(long toId, long fromId) {
        String sql = "INSERT INTO messenger_requests (to_id, from_id) VALUES (?, ?)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, toId);
            stmt.setLong(2, fromId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
                // Fallback: query for the request ID
                return getRequestId(toId, fromId);
            }
        } catch (SQLException e) {
            logger.error("Failed to create request: {}", e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Gets the ID of a request.
     * @param toId To user ID
     * @param fromId From user ID
     * @return Request ID, or 0 if not found
     */
    public long getRequestId(long toId, long fromId) {
        String sql = """
            SELECT id FROM messenger_requests WHERE to_id = ? AND from_id = ?
            ORDER BY id DESC LIMIT 1
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, toId);
            stmt.setLong(2, fromId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get request ID: {}", e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Gets user info by username (for friend requests).
     * @param username Username
     * @return User data (id, block_newfriends), or null if not found
     */
    public Map<String, Object> getUserByUsername(String username) {
        String sql = "SELECT id, block_newfriends FROM users WHERE username = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username.toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", rs.getLong("id"));
                    user.put("block_newfriends", rs.getString("block_newfriends"));
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user by username: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Searches for users by username pattern.
     * @param query Username query pattern
     * @return List of user IDs matching the query
     */
    public List<Long> searchUsers(String query) {
        String sql = "SELECT id FROM users WHERE username LIKE ? LIMIT 50";
        List<Long> userIds = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + query + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to search users: {}", e.getMessage(), e);
        }
        
        return userIds;
    }
    
    /**
     * Gets username by user ID.
     * @param userId User ID
     * @return Username, or null if not found
     */
    public String getUsername(long userId) {
        String sql = "SELECT username FROM users WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get username for user {}: {}", userId, e.getMessage(), e);
        }
        
        return null;
    }
}
