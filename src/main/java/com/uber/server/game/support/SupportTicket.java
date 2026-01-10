package com.uber.server.game.support;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.ModerationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a support ticket.
 */
public class SupportTicket {
    private static final Logger logger = LoggerFactory.getLogger(SupportTicket.class);
    
    private final long id;
    private int score;
    private final int type;
    private TicketStatus status;
    private final long senderId;
    private final long reportedId;
    private long moderatorId;
    private final String message;
    private final long roomId;
    private final String roomName;
    private final long timestamp;
    
    private final ModerationRepository moderationRepository;
    
    public SupportTicket(long id, int score, int type, long senderId, long reportedId, 
                        String message, long roomId, String roomName, long timestamp,
                        ModerationRepository moderationRepository) {
        this.id = id;
        this.score = score;
        this.type = type;
        this.status = TicketStatus.OPEN;
        this.senderId = senderId;
        this.reportedId = reportedId;
        this.moderatorId = 0;
        this.message = message;
        this.roomId = roomId;
        this.roomName = roomName;
        this.timestamp = timestamp;
        this.moderationRepository = moderationRepository;
    }
    
    /**
     * Gets the tab ID for this ticket based on status.
     * @return Tab ID (1 = open, 2 = picked, 0 = other)
     */
    public int getTabId() {
        if (status == TicketStatus.OPEN) {
            return 1;
        }
        if (status == TicketStatus.PICKED) {
            return 2;
        }
        return 0;
    }
    
    /**
     * Picks this ticket (assigns it to a moderator).
     * @param moderatorId Moderator ID
     * @param updateInDb If true, updates database
     */
    public void pick(long moderatorId, boolean updateInDb) {
        this.status = TicketStatus.PICKED;
        this.moderatorId = moderatorId;
        
        if (updateInDb && moderationRepository != null) {
            if (!moderationRepository.pickTicket(id, moderatorId)) {
                logger.error("Failed to pick ticket {} in database", id);
            }
        }
    }
    
    /**
     * Closes this ticket with a new status.
     * @param newStatus New ticket status (RESOLVED, ABUSIVE, INVALID)
     * @param updateInDb If true, updates database
     */
    public void close(TicketStatus newStatus, boolean updateInDb) {
        this.status = newStatus;
        
        if (updateInDb && moderationRepository != null) {
            String dbStatus;
            switch (newStatus) {
                case ABUSIVE:
                    dbStatus = "abusive";
                    break;
                case INVALID:
                    dbStatus = "invalid";
                    break;
                case RESOLVED:
                default:
                    dbStatus = "resolved";
                    break;
            }
            
            if (!moderationRepository.updateTicketStatus(id, dbStatus, moderatorId)) {
                logger.error("Failed to close ticket {} in database", id);
            }
        }
    }
    
    /**
     * Releases this ticket (sets status back to OPEN).
     * @param updateInDb If true, updates database
     */
    public void release(boolean updateInDb) {
        this.status = TicketStatus.OPEN;
        this.moderatorId = 0;
        
        if (updateInDb && moderationRepository != null) {
            if (!moderationRepository.updateTicketStatus(id, "open", 0)) {
                logger.error("Failed to release ticket {} in database", id);
            }
        }
    }
    
    /**
     * Deletes this ticket.
     * @param updateInDb If true, updates database
     */
    public void delete(boolean updateInDb) {
        this.status = TicketStatus.DELETED;
        
        if (updateInDb && moderationRepository != null) {
            if (!moderationRepository.updateTicketStatus(id, "deleted", moderatorId)) {
                logger.error("Failed to delete ticket {} in database", id);
            }
        }
    }
    
    /**
     * Serializes the ticket to a ServerMessage.
     * @param game Game instance (for getting client manager)
     * @return ServerMessage with ticket data
     */
    public ServerMessage serialize(Game game) {
        ServerMessage message = new ServerMessage(530);
        message.appendUInt(id);
        message.appendInt32(getTabId());
        message.appendInt32(11); // ??
        message.appendInt32(type);
        message.appendInt32(11); // ??
        message.appendInt32(score);
        message.appendUInt(senderId);
        
        // Get sender name (try online first, then repository)
        String senderName = getUsernameById(game, senderId);
        message.appendStringWithBreak(senderName != null ? senderName : "");
        
        message.appendUInt(reportedId);
        
        // Get reported name
        String reportedName = getUsernameById(game, reportedId);
        message.appendStringWithBreak(reportedName != null ? reportedName : "");
        
        message.appendUInt(moderatorId);
        
        // Get moderator name
        String moderatorName = "";
        if (moderatorId > 0) {
            moderatorName = getUsernameById(game, moderatorId);
        }
        message.appendStringWithBreak(moderatorName != null ? moderatorName : "");
        
        message.appendStringWithBreak(this.message);
        message.appendUInt(roomId);
        message.appendStringWithBreak(roomName);
        
        return message;
    }
    
    // Getters and setters
    public long getId() { return id; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getType() { return type; }
    public TicketStatus getStatus() { return status; }
    public long getSenderId() { return senderId; }
    public long getReportedId() { return reportedId; }
    public long getModeratorId() { return moderatorId; }
    public String getMessage() { return message; }
    public long getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public long getTimestamp() { return timestamp; }
    
    /**
     * Gets username by user ID, checking online clients first, then repository.
     * @param game Game instance
     * @param userId User ID
     * @return Username, or null if not found
     */
    private String getUsernameById(Game game, long userId) {
        if (game == null || userId <= 0) {
            return null;
        }
        
        // Try online client first
        if (game.getClientManager() != null) {
            GameClient client = game.getClientManager().getClientByHabbo(userId);
            if (client != null && client.getHabbo() != null) {
                return client.getHabbo().getUsername();
            }
        }
        
        // Fallback to repository
        if (game.getUserRepository() != null) {
            return game.getUserRepository().getRealName(userId);
        }
        
        return null;
    }
}
