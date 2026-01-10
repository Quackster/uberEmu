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
 * Repository for moderation database operations.
 */
public class ModerationRepository {
    private static final Logger logger = LoggerFactory.getLogger(ModerationRepository.class);
    private final DatabasePool databasePool;
    
    public ModerationRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads moderation message presets.
     * @return Map with "user" and "room" keys containing lists of preset messages
     */
    public Map<String, List<String>> loadMessagePresets() {
        String sql = "SELECT type, message FROM moderation_presets WHERE enabled = '1'";
        Map<String, List<String>> presets = new HashMap<>();
        presets.put("user", new ArrayList<>());
        presets.put("room", new ArrayList<>());
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String type = rs.getString("type").toLowerCase();
                String message = rs.getString("message");
                
                if ("message".equals(type)) {
                    presets.get("user").add(message);
                } else if ("roommessage".equals(type)) {
                    presets.get("room").add(message);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load moderation presets: {}", e.getMessage(), e);
        }
        
        return presets;
    }
    
    /**
     * Loads pending support tickets.
     * @return List of ticket data
     */
    public List<Map<String, Object>> loadPendingTickets() {
        String sql = """
            SELECT id, score, type, status, sender_id, reported_id, moderator_id, message, room_id, room_name, timestamp
            FROM moderation_tickets
            WHERE status = 'open' OR status = 'picked'
            """;
        List<Map<String, Object>> tickets = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> ticket = new HashMap<>();
                ticket.put("id", rs.getLong("id"));
                ticket.put("score", rs.getInt("score"));
                ticket.put("type", rs.getInt("type"));
                ticket.put("status", rs.getString("status"));
                ticket.put("sender_id", rs.getLong("sender_id"));
                ticket.put("reported_id", rs.getLong("reported_id"));
                ticket.put("moderator_id", rs.getLong("moderator_id"));
                ticket.put("message", rs.getString("message"));
                ticket.put("room_id", rs.getLong("room_id"));
                ticket.put("room_name", rs.getString("room_name"));
                ticket.put("timestamp", rs.getLong("timestamp"));
                tickets.add(ticket);
            }
        } catch (SQLException e) {
            logger.error("Failed to load pending tickets: {}", e.getMessage(), e);
        }
        
        return tickets;
    }
    
    /**
     * Creates a new support ticket.
     * @param score Ticket score
     * @param category Ticket category/type
     * @param senderId Sender user ID
     * @param reportedId Reported user ID
     * @param message Ticket message
     * @param roomId Room ID
     * @param roomName Room name
     * @param timestamp Timestamp
     * @return The ID of the created ticket, or 0 if failed
     */
    public long createTicket(int score, int category, long senderId, long reportedId, 
                            String message, long roomId, String roomName, long timestamp) {
        String sql = """
            INSERT INTO moderation_tickets (score, type, status, sender_id, reported_id, moderator_id, message, room_id, room_name, timestamp)
            VALUES (?, ?, 'open', ?, ?, 0, ?, ?, ?, ?)
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, score);
            stmt.setInt(2, category);
            stmt.setLong(3, senderId);
            stmt.setLong(4, reportedId);
            stmt.setString(5, message);
            stmt.setLong(6, roomId);
            stmt.setString(7, roomName);
            stmt.setLong(8, timestamp);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to create ticket: {}", e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Gets the most recent ticket ID for a sender.
     * @param senderId Sender user ID
     * @return Ticket ID, or 0 if not found
     */
    public long getLatestTicketId(long senderId) {
        String sql = "SELECT id FROM moderation_tickets WHERE sender_id = ? ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, senderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get latest ticket ID for sender {}: {}", senderId, e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Updates ticket status.
     * @param ticketId Ticket ID
     * @param status New status
     * @param moderatorId Moderator ID (0 if unpicking)
     * @return True if update was successful
     */
    public boolean updateTicketStatus(long ticketId, String status, long moderatorId) {
        String sql = "UPDATE moderation_tickets SET status = ?, moderator_id = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setLong(2, moderatorId);
            stmt.setLong(3, ticketId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update ticket status: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Closes a ticket.
     * @param ticketId Ticket ID
     * @param moderatorId Moderator ID who closed it
     * @return True if update was successful
     */
    public boolean closeTicket(long ticketId, long moderatorId) {
        return updateTicketStatus(ticketId, "closed", moderatorId);
    }
    
    /**
     * Picks a ticket (assigns it to a moderator).
     * @param ticketId Ticket ID
     * @param moderatorId Moderator ID
     * @return True if update was successful
     */
    public boolean pickTicket(long ticketId, long moderatorId) {
        return updateTicketStatus(ticketId, "picked", moderatorId);
    }
    
    /**
     * Gets room visits for a user (with timestamps for chat log lookup).
     * @param userId User ID
     * @param limit Maximum number of visits to retrieve
     * @return List of room visit data with entry/exit timestamps
     */
    public List<Map<String, Object>> getUserRoomVisits(long userId, int limit) {
        String sql = """
            SELECT room_id, entry_timestamp, exit_timestamp, hour, minute
            FROM user_roomvisits WHERE user_id = ? ORDER BY entry_timestamp DESC LIMIT ?
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
                    visit.put("entry_timestamp", rs.getLong("entry_timestamp"));
                    long exitTimestamp = rs.getLong("exit_timestamp");
                    if (exitTimestamp <= 0) {
                        exitTimestamp = com.uber.server.util.TimeUtil.getUnixTimestamp();
                    }
                    visit.put("exit_timestamp", exitTimestamp);
                    visit.put("hour", rs.getInt("hour"));
                    visit.put("minute", rs.getInt("minute"));
                    visits.add(visit);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get room visits for user {}: {}", userId, e.getMessage(), e);
        }
        
        return visits;
    }
}
