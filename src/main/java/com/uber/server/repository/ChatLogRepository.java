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
 * Repository for chat log database operations.
 */
public class ChatLogRepository {
    private static final Logger logger = LoggerFactory.getLogger(ChatLogRepository.class);
    private final DatabasePool databasePool;
    
    public ChatLogRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Logs a chat message.
     * @param userId User ID
     * @param roomId Room ID
     * @param hour Hour (0-23)
     * @param minute Minute (0-59)
     * @param timestamp Unix timestamp
     * @param message Chat message
     * @param userName Username
     * @param fullDate Full date string
     * @return True if logging was successful
     */
    public boolean logChat(long userId, long roomId, int hour, int minute, long timestamp, 
                          String message, String userName, String fullDate) {
        String sql = """
            INSERT INTO chatlogs (user_id, room_id, hour, minute, timestamp, message, user_name, full_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, roomId);
            stmt.setInt(3, hour);
            stmt.setInt(4, minute);
            stmt.setLong(5, timestamp);
            stmt.setString(6, message);
            stmt.setString(7, userName);
            stmt.setString(8, fullDate);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to log chat message: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets chat logs for a room.
     * @param roomId Room ID
     * @param limit Maximum number of logs to retrieve
     * @return List of chat log entries
     */
    public List<Map<String, Object>> getRoomChatLogs(long roomId, int limit) {
        String sql = "SELECT * FROM chatlogs WHERE room_id = ? ORDER BY timestamp DESC LIMIT ?";
        List<Map<String, Object>> logs = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> log = new HashMap<>();
                    log.put("user_id", rs.getLong("user_id"));
                    log.put("room_id", rs.getLong("room_id"));
                    log.put("hour", rs.getInt("hour"));
                    log.put("minute", rs.getInt("minute"));
                    log.put("timestamp", rs.getLong("timestamp"));
                    log.put("message", rs.getString("message"));
                    log.put("user_name", rs.getString("user_name"));
                    log.put("full_date", rs.getString("full_date"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get chat logs for room {}: {}", roomId, e.getMessage(), e);
        }
        
        return logs;
    }
    
    /**
     * Gets chat logs for a specific user in a room.
     * @param userId User ID
     * @param roomId Room ID
     * @param limit Maximum number of logs to retrieve
     * @return List of chat log entries
     */
    public List<Map<String, Object>> getUserRoomChatLogs(long userId, long roomId, int limit) {
        String sql = "SELECT * FROM chatlogs WHERE user_id = ? AND room_id = ? ORDER BY timestamp DESC LIMIT ?";
        List<Map<String, Object>> logs = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, roomId);
            stmt.setInt(3, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> log = new HashMap<>();
                    log.put("user_id", rs.getLong("user_id"));
                    log.put("room_id", rs.getLong("room_id"));
                    log.put("hour", rs.getInt("hour"));
                    log.put("minute", rs.getInt("minute"));
                    log.put("timestamp", rs.getLong("timestamp"));
                    log.put("message", rs.getString("message"));
                    log.put("user_name", rs.getString("user_name"));
                    log.put("full_date", rs.getString("full_date"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get chat logs for user {} in room {}: {}", userId, roomId, e.getMessage(), e);
        }
        
        return logs;
    }
    
    /**
     * Gets chat logs for a room within a time range for a specific user.
     * @param roomId Room ID
     * @param userId User ID
     * @param startTimestamp Start timestamp (inclusive)
     * @param endTimestamp End timestamp (inclusive)
     * @return List of chat log entries
     */
    public List<Map<String, Object>> getUserRoomChatLogsInRange(long roomId, long userId, long startTimestamp, long endTimestamp) {
        String sql = """
            SELECT user_id, user_name, hour, minute, message FROM chatlogs
            WHERE room_id = ? AND user_id = ? AND timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC
            """;
        List<Map<String, Object>> logs = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            stmt.setLong(2, userId);
            stmt.setLong(3, startTimestamp);
            stmt.setLong(4, endTimestamp);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> log = new HashMap<>();
                    log.put("user_id", rs.getLong("user_id"));
                    log.put("user_name", rs.getString("user_name"));
                    log.put("hour", rs.getInt("hour"));
                    log.put("minute", rs.getInt("minute"));
                    log.put("message", rs.getString("message"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get chat logs for user {} in room {} in range {}-{}: {}", 
                        userId, roomId, startTimestamp, endTimestamp, e.getMessage(), e);
        }
        
        return logs;
    }
    
    /**
     * Gets chat logs for a room in a time range (for all users).
     * @param roomId Room ID
     * @param startTimestamp Start timestamp (inclusive)
     * @param endTimestamp End timestamp (inclusive)
     * @return List of chat log entries
     */
    public List<Map<String, Object>> getRoomChatLogsInTimeRange(long roomId, long startTimestamp, long endTimestamp) {
        String sql = """
            SELECT user_id, user_name, hour, minute, message FROM chatlogs
            WHERE room_id = ? AND timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC
            """;
        List<Map<String, Object>> logs = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            stmt.setLong(2, startTimestamp);
            stmt.setLong(3, endTimestamp);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> log = new HashMap<>();
                    log.put("user_id", rs.getLong("user_id"));
                    log.put("user_name", rs.getString("user_name"));
                    log.put("hour", rs.getInt("hour"));
                    log.put("minute", rs.getInt("minute"));
                    log.put("message", rs.getString("message"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get chat logs for room {} in range {}-{}: {}", 
                        roomId, startTimestamp, endTimestamp, e.getMessage(), e);
        }
        
        return logs;
    }
    
    /**
     * Gets room visits with chat logs for a user.
     * Groups by room_id and returns distinct rooms with their chat log counts.
     * @param userId User ID
     * @param limit Maximum number of rooms to return
     * @return List of room visit maps with room_id and chat log count
     */
    public List<Map<String, Object>> getUserRoomVisitsWithChatLogs(long userId, int limit) {
        String sql = """
            SELECT room_id, COUNT(*) as chat_count, MAX(timestamp) as last_visit
            FROM chatlogs WHERE user_id = ? GROUP BY room_id ORDER BY last_visit DESC LIMIT ?
            """;
        List<Map<String, Object>> visits = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> visit = new HashMap<>();
                    visit.put("room_id", rs.getLong("room_id"));
                    visit.put("chat_count", rs.getInt("chat_count"));
                    visit.put("last_visit", rs.getLong("last_visit"));
                    visits.add(visit);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get room visits with chat logs for user {}: {}", userId, e.getMessage(), e);
        }
        
        return visits;
    }
}
