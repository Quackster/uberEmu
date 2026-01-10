package com.uber.server.repository;

import com.uber.server.storage.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for user info database operations.
 */
public class UserInfoRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserInfoRepository.class);
    private final DatabasePool databasePool;
    
    public UserInfoRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Increments CFHS (calls for help) count for a user.
     * @param userId User ID
     * @return True if update was successful
     */
    public boolean incrementCfhs(long userId) {
        String sql = "UPDATE user_info SET cfhs = cfhs + 1 WHERE user_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to increment CFHS for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets user info statistics.
     * @param userId User ID
     * @return Map containing user info statistics (cfhs, bans, cautions, etc.), or null if not found
     */
    public Map<String, Object> getUserInfo(long userId) {
        String sql = "SELECT * FROM user_info WHERE user_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("user_id", rs.getLong("user_id"));
                    info.put("cfhs", rs.getInt("cfhs"));
                    info.put("cfhs_abusive", rs.getInt("cfhs_abusive"));
                    info.put("bans", rs.getInt("bans"));
                    info.put("cautions", rs.getInt("cautions"));
                    info.put("reg_timestamp", rs.getLong("reg_timestamp"));
                    info.put("login_timestamp", rs.getLong("login_timestamp"));
                    return info;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user info for user {}: {}", userId, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Creates user info entry if it doesn't exist.
     * @param userId User ID
     * @return True if creation was successful or already exists
     */
    public boolean ensureUserInfoExists(long userId) {
        // Try to get existing entry first
        if (getUserInfo(userId) != null) {
            return true;
        }
        
        // Create new entry
        String sql = "INSERT INTO user_info (user_id, cfhs, cfhs_abusive, bans, cautions, reg_timestamp, login_timestamp) " +
                    "VALUES (?, 0, 0, 0, 0, 0, 0)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // If it's a duplicate key error, that's fine - entry already exists
            if (e.getErrorCode() == 1062) { // MySQL duplicate key error
                return true;
            }
            logger.error("Failed to create user info for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates login timestamp.
     * @param userId User ID
     * @param timestamp Login timestamp
     * @return True if update was successful
     */
    public boolean updateLoginTimestamp(long userId, long timestamp) {
        // Ensure entry exists first
        ensureUserInfoExists(userId);
        
        String sql = "UPDATE user_info SET login_timestamp = ? WHERE user_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, timestamp);
            stmt.setLong(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update login timestamp for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Increments ban count for a user.
     * @param userId User ID
     * @return True if update was successful
     */
    public boolean incrementBanCount(long userId) {
        // Ensure entry exists first
        ensureUserInfoExists(userId);
        
        String sql = "UPDATE user_info SET bans = bans + 1 WHERE user_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to increment ban count for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Increments caution count for a user.
     * @param userId User ID
     * @return True if update was successful
     */
    public boolean incrementCaution(long userId) {
        // Ensure entry exists first
        ensureUserInfoExists(userId);
        
        String sql = "UPDATE user_info SET cautions = cautions + 1 WHERE user_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to increment caution for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Increments abusive CFHS count for a user.
     * @param userId User ID
     * @return True if update was successful
     */
    public boolean incrementCfhsAbusive(long userId) {
        // Ensure entry exists first
        ensureUserInfoExists(userId);
        
        String sql = "UPDATE user_info SET cfhs_abusive = cfhs_abusive + 1 WHERE user_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to increment abusive CFHS for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
}
