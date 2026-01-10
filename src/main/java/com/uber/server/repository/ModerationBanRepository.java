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
 * Repository for moderation ban database operations.
 */
public class ModerationBanRepository {
    private static final Logger logger = LoggerFactory.getLogger(ModerationBanRepository.class);
    private final DatabasePool databasePool;
    
    public ModerationBanRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Creates a new ban.
     * @param banType Ban type (e.g., "ip", "user")
     * @param value Ban value (IP address or user ID)
     * @param reason Ban reason
     * @param expire Expiration timestamp
     * @param addedBy Moderator user ID who added the ban
     * @param addedDate Date string when ban was added
     * @return True if creation was successful
     */
    public boolean createBan(String banType, String value, String reason, long expire, long addedBy, String addedDate) {
        String sql = """
            INSERT INTO bans (bantype, value, reason, expire, added_by, added_date)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, banType);
            stmt.setString(2, value);
            stmt.setString(3, reason);
            stmt.setLong(4, expire);
            stmt.setLong(5, addedBy);
            stmt.setString(6, addedDate);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to create ban: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets all users with a specific IP address.
     * @param ipAddress IP address
     * @return List of user IDs
     */
    public List<Long> getUsersByIp(String ipAddress) {
        String sql = "SELECT id FROM users WHERE ip_last = ?";
        List<Long> userIds = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, ipAddress);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get users by IP: {}", e.getMessage(), e);
        }
        
        return userIds;
    }
    
    /**
     * Checks if a user is banned.
     * @param userId User ID
     * @return Ban data if user is banned, or null
     */
    public Map<String, Object> getUserBan(long userId) {
        String sql = "SELECT * FROM bans WHERE bantype = 'user' AND value = ? AND expire > ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, String.valueOf(userId));
            stmt.setLong(2, System.currentTimeMillis() / 1000); // Current Unix timestamp
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> ban = new HashMap<>();
                    ban.put("bantype", rs.getString("bantype"));
                    ban.put("value", rs.getString("value"));
                    ban.put("reason", rs.getString("reason"));
                    ban.put("expire", rs.getLong("expire"));
                    ban.put("added_by", rs.getLong("added_by"));
                    ban.put("added_date", rs.getString("added_date"));
                    return ban;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user ban: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Checks if an IP address is banned.
     * @param ipAddress IP address
     * @return Ban data if IP is banned, or null
     */
    public Map<String, Object> getIpBan(String ipAddress) {
        String sql = "SELECT * FROM bans WHERE bantype = 'ip' AND value = ? AND expire > ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, ipAddress);
            stmt.setLong(2, System.currentTimeMillis() / 1000); // Current Unix timestamp
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> ban = new HashMap<>();
                    ban.put("bantype", rs.getString("bantype"));
                    ban.put("value", rs.getString("value"));
                    ban.put("reason", rs.getString("reason"));
                    ban.put("expire", rs.getLong("expire"));
                    ban.put("added_by", rs.getLong("added_by"));
                    ban.put("added_date", rs.getString("added_date"));
                    return ban;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get IP ban: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Gets user info by username for ban lookup.
     * @param username Username
     * @return User data (id, ip_last), or null if not found
     */
    public Map<String, Object> getUserByUsername(String username) {
        String sql = "SELECT id, ip_last FROM users WHERE username = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", rs.getLong("id"));
                    user.put("ip_last", rs.getString("ip_last"));
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user by username: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Loads all active bans (not expired).
     * @return List of ban data (bantype, value, reason, expire)
     */
    public List<Map<String, Object>> loadBans() {
        String sql = "SELECT bantype, value, reason, expire FROM bans WHERE expire > ?";
        List<Map<String, Object>> bans = new ArrayList<>();
        
        long currentTime = System.currentTimeMillis() / 1000; // Current Unix timestamp
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, currentTime);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> ban = new HashMap<>();
                    ban.put("bantype", rs.getString("bantype"));
                    ban.put("value", rs.getString("value"));
                    ban.put("reason", rs.getString("reason"));
                    ban.put("expire", rs.getLong("expire"));
                    bans.add(ban);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load bans: {}", e.getMessage(), e);
        }
        
        return bans;
    }
}
