package com.uber.server.game.support;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.ChatLogRepository;
import com.uber.server.repository.ModerationRepository;
import com.uber.server.repository.UserInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Moderation tool for staff members.
 */
public class ModerationTool {
    private static final Logger logger = LoggerFactory.getLogger(ModerationTool.class);
    
    private final List<SupportTicket> tickets;
    private final List<String> userMessagePresets;
    private final List<String> roomMessagePresets;
    
    private final ModerationRepository moderationRepository;
    private final UserInfoRepository userInfoRepository;
    private final ChatLogRepository chatLogRepository;
    private final Game game;
    
    public ModerationTool(ModerationRepository moderationRepository,
                         UserInfoRepository userInfoRepository,
                         ChatLogRepository chatLogRepository,
                         Game game) {
        this.tickets = new CopyOnWriteArrayList<>();
        this.userMessagePresets = new CopyOnWriteArrayList<>();
        this.roomMessagePresets = new CopyOnWriteArrayList<>();
        
        this.moderationRepository = moderationRepository;
        this.userInfoRepository = userInfoRepository;
        this.chatLogRepository = chatLogRepository;
        this.game = game;
    }
    
    /**
     * Loads message presets from the database.
     */
    public void loadMessagePresets() {
        userMessagePresets.clear();
        roomMessagePresets.clear();
        
        Map<String, List<String>> presets = moderationRepository.loadMessagePresets();
        userMessagePresets.addAll(presets.get("user"));
        roomMessagePresets.addAll(presets.get("room"));
        
        logger.info("Loaded {} user presets and {} room presets", 
                   userMessagePresets.size(), roomMessagePresets.size());
    }
    
    /**
     * Loads pending support tickets from the database.
     */
    public void loadPendingTickets() {
        tickets.clear();
        
        List<Map<String, Object>> ticketData = moderationRepository.loadPendingTickets();
        for (Map<String, Object> row : ticketData) {
            try {
                long id = ((Number) row.get("id")).longValue();
                int score = ((Number) row.get("score")).intValue();
                int type = ((Number) row.get("type")).intValue();
                long senderId = ((Number) row.get("sender_id")).longValue();
                long reportedId = ((Number) row.get("reported_id")).longValue();
                String message = (String) row.get("message");
                long roomId = ((Number) row.get("room_id")).longValue();
                String roomName = (String) row.get("room_name");
                long timestamp = ((Number) row.get("timestamp")).longValue();
                
                SupportTicket ticket = new SupportTicket(id, score, type, senderId, reportedId,
                                                         message, roomId, roomName, timestamp,
                                                         moderationRepository);
                
                // Check if ticket was picked
                String status = (String) row.get("status");
                if ("picked".equalsIgnoreCase(status)) {
                    long moderatorId = ((Number) row.get("moderator_id")).longValue();
                    ticket.pick(moderatorId, false);
                }
                
                tickets.add(ticket);
            } catch (Exception e) {
                logger.error("Failed to load ticket: {}", e.getMessage(), e);
            }
        }
        
        logger.info("Loaded {} pending tickets", tickets.size());
    }
    
    /**
     * Serializes the moderation tool for a moderator session.
     * @return ServerMessage with tool data
     */
    public ServerMessage serializeTool() {
        ServerMessage message = new ServerMessage(531);
        message.appendInt32(-1);
        message.appendInt32(userMessagePresets.size());
        
        for (String preset : userMessagePresets) {
            message.appendStringWithBreak(preset);
        }
        
        message.appendInt32(0);
        message.appendInt32(14);
        message.appendInt32(1);
        message.appendInt32(1);
        message.appendInt32(1);
        message.appendInt32(1);
        message.appendInt32(1);
        message.appendInt32(1);
        
        message.appendInt32(roomMessagePresets.size());
        
        for (String preset : roomMessagePresets) {
            message.appendStringWithBreak(preset);
        }
        
        // Append hardcoded preset values
        for (int i = 0; i < 20; i++) {
            message.appendInt32(1);
        }
        message.appendStringWithBreak("test");
        
        for (int i = 0; i < 11; i++) {
            message.appendInt32(1);
        }
        message.appendStringWithBreak("test");
        
        for (int i = 0; i < 11; i++) {
            message.appendInt32(1);
        }
        message.appendStringWithBreak("test");
        
        return message;
    }
    
    /**
     * Gets a ticket by ID.
     * @param ticketId Ticket ID
     * @return SupportTicket object, or null if not found
     */
    public SupportTicket getTicket(long ticketId) {
        for (SupportTicket ticket : tickets) {
            if (ticket.getId() == ticketId) {
                return ticket;
            }
        }
        return null;
    }
    
    /**
     * Sends open tickets to a moderator session.
     * @param session GameClient session (must be moderator)
     */
    public void sendOpenTickets(GameClient session) {
        if (session == null || session.getHabbo() == null) {
            return;
        }
        
        for (SupportTicket ticket : tickets) {
            TicketStatus status = ticket.getStatus();
            if (status == TicketStatus.OPEN || status == TicketStatus.PICKED) {
                session.sendMessage(ticket.serialize(game));
            }
        }
    }
    
    /**
     * Checks if a user has a pending ticket.
     * @param userId User ID
     * @return True if user has a pending ticket
     */
    public boolean userHasPendingTicket(long userId) {
        for (SupportTicket ticket : tickets) {
            if (ticket.getSenderId() == userId && ticket.getStatus() == TicketStatus.OPEN) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets all tickets.
     * @return List of all tickets
     */
    public List<SupportTicket> getTickets() {
        return new ArrayList<>(tickets);
    }
    
    /**
     * Gets user message presets.
     * @return List of user message presets
     */
    public List<String> getUserMessagePresets() {
        return new ArrayList<>(userMessagePresets);
    }
    
    /**
     * Gets room message presets.
     * @return List of room message presets
     */
    public List<String> getRoomMessagePresets() {
        return new ArrayList<>(roomMessagePresets);
    }
    
    /**
     * Sends a new support ticket.
     * @param session GameClient session
     * @param category Ticket category/type
     * @param reportedUserId Reported user ID
     * @param message Ticket message
     */
    public void sendNewTicket(GameClient session, int category, long reportedUserId, String message) {
        if (session == null || session.getHabbo() == null || session.getHabbo().getCurrentRoomId() <= 0) {
            return;
        }
        
        com.uber.server.game.rooms.RoomData roomData = game.getRoomManager().generateNullableRoomData(session.getHabbo().getCurrentRoomId());
        if (roomData == null) {
            return;
        }
        
        // Create ticket in database
        long timestamp = com.uber.server.util.TimeUtil.getUnixTimestamp();
        long ticketId = moderationRepository.createTicket(1, category, session.getHabbo().getId(), 
                                                         reportedUserId, message, roomData.getId(), 
                                                         roomData.getName(), timestamp);
        
        // If createTicket didn't return the ID, get it from the database
        if (ticketId <= 0) {
            ticketId = moderationRepository.getLatestTicketId(session.getHabbo().getId());
        }
        
        if (ticketId <= 0) {
            logger.error("Failed to create ticket for user {}", session.getHabbo().getId());
            return;
        }
        
        // Increment user's Call for Help count
        if (userInfoRepository != null) {
            userInfoRepository.incrementCfhs(session.getHabbo().getId());
        }
        
        // Create SupportTicket object
        SupportTicket ticket = new SupportTicket(ticketId, 1, category, session.getHabbo().getId(), 
                                                 reportedUserId, message, roomData.getId(), 
                                                 roomData.getName(), timestamp, moderationRepository);
        tickets.add(ticket);
        
        // Send ticket to all moderators
        sendTicketToModerators(ticket);
    }
    
    /**
     * Deletes a pending ticket for a user.
     * @param userId User ID
     */
    public void deletePendingTicketForUser(long userId) {
        for (SupportTicket ticket : tickets) {
            if (ticket.getSenderId() == userId && ticket.getStatus() == TicketStatus.OPEN) {
                ticket.delete(true);
                sendTicketToModerators(ticket);
                return;
            }
        }
    }
    
    /**
     * Picks a ticket (assigns it to a moderator).
     * @param session Moderator session
     * @param ticketId Ticket ID
     */
    public void pickTicket(GameClient session, long ticketId) {
        if (session == null || session.getHabbo() == null) {
            return;
        }
        
        SupportTicket ticket = getTicket(ticketId);
        if (ticket == null || ticket.getStatus() != TicketStatus.OPEN) {
            return;
        }
        
        ticket.pick(session.getHabbo().getId(), true);
        sendTicketToModerators(ticket);
    }
    
    /**
     * Releases a ticket (sets status back to OPEN).
     * @param session Moderator session
     * @param ticketId Ticket ID
     */
    public void releaseTicket(GameClient session, long ticketId) {
        if (session == null || session.getHabbo() == null) {
            return;
        }
        
        SupportTicket ticket = getTicket(ticketId);
        if (ticket == null || ticket.getStatus() != TicketStatus.PICKED || ticket.getModeratorId() != session.getHabbo().getId()) {
            return;
        }
        
        ticket.release(true);
        sendTicketToModerators(ticket);
    }
    
    /**
     * Closes a ticket with a result.
     * @param session Moderator session
     * @param ticketId Ticket ID
     * @param result Result code (1 = invalid, 2 = abusive, 3 = resolved)
     */
    public void closeTicket(GameClient session, long ticketId, int result) {
        if (session == null || session.getHabbo() == null) {
            return;
        }
        
        SupportTicket ticket = getTicket(ticketId);
        if (ticket == null || ticket.getStatus() != TicketStatus.PICKED || ticket.getModeratorId() != session.getHabbo().getId()) {
            return;
        }
        
        // Get sender client to send result message
        GameClient senderClient = game.getClientManager().getClientByHabbo(ticket.getSenderId());
        
        var resultPair = switch (result) {
            case 1 -> {
                yield new java.util.AbstractMap.SimpleEntry<>(1, TicketStatus.INVALID);
            }
            case 2 -> {
                // Increment abusive Call for Help count
                if (userInfoRepository != null) {
                    userInfoRepository.incrementCfhsAbusive(ticket.getSenderId());
                }
                yield new java.util.AbstractMap.SimpleEntry<>(2, TicketStatus.ABUSIVE);
            }
            default -> new java.util.AbstractMap.SimpleEntry<>(0, TicketStatus.RESOLVED);
        };
        int resultCode = resultPair.getKey();
        TicketStatus newStatus = resultPair.getValue();
        
        // Send result message to sender
        if (senderClient != null) {
            var composer = new com.uber.server.messages.outgoing.support.IssueCloseNotificationComposer(resultCode);
            senderClient.sendMessage(composer.compose());
        }
        
        ticket.close(newStatus, true);
        sendTicketToModerators(ticket);
    }
    
    /**
     * Sends a ticket update to all moderators.
     * @param ticket SupportTicket to broadcast
     */
    public void sendTicketToModerators(SupportTicket ticket) {
        if (ticket == null || game == null || game.getClientManager() == null) {
            return;
        }
        
        ServerMessage message = ticket.serialize(game);
        game.getClientManager().broadcastMessage(message, "fuse_mod");
    }
    
    /**
     * Serializes user info for moderation.
     * @param userId User ID
     * @return ServerMessage with user info (ID 533)
     */
    public ServerMessage serializeUserInfo(long userId) {
        if (game == null || game.getUserRepository() == null) {
            throw new IllegalArgumentException("Cannot serialize user info: repositories not available");
        }
        
        // Get user data
        Map<String, Object> userData = game != null && game.getUserRepository() != null ?
                                      game.getUserRepository().getUser(userId) : null;
        if (userData == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        // Get user info
        Map<String, Object> info = userInfoRepository != null ? userInfoRepository.getUserInfo(userId) : null;
        
        ServerMessage message = new ServerMessage(533);
        message.appendUInt(userId);
        String username = (String) userData.get("username");
        message.appendStringWithBreak(username != null ? username : "");
        
        if (info != null) {
            long regTimestamp = ((Number) info.get("reg_timestamp")).longValue();
            long loginTimestamp = ((Number) info.get("login_timestamp")).longValue();
            long currentTimestamp = com.uber.server.util.TimeUtil.getUnixTimestamp();
            message.appendInt32((int) Math.ceil((currentTimestamp - regTimestamp) / 60.0));
            message.appendInt32((int) Math.ceil((currentTimestamp - loginTimestamp) / 60.0));
        } else {
            message.appendInt32(0);
            message.appendInt32(0);
        }
        
        boolean online = ((Number) userData.get("online")).intValue() == 1;
        message.appendBoolean(online);
        
        if (info != null) {
            message.appendInt32(((Number) info.get("cfhs")).intValue());
            message.appendInt32(((Number) info.get("cfhs_abusive")).intValue());
            message.appendInt32(((Number) info.get("cautions")).intValue());
            message.appendInt32(((Number) info.get("bans")).intValue());
        } else {
            message.appendInt32(0); // cfhs
            message.appendInt32(0); // abusive cfhs
            message.appendInt32(0); // cautions
            message.appendInt32(0); // bans
        }
        
        return message;
    }
    
    /**
     * Serializes room visits for a user.
     * @param userId User ID
     * @return ServerMessage with room visits (ID 537)
     */
    public ServerMessage serializeRoomVisits(long userId) {
        List<Map<String, Object>> visits = moderationRepository != null ? 
                                          moderationRepository.getUserRoomVisits(userId, 50) : 
                                          new ArrayList<>();
        
        ServerMessage message = new ServerMessage(537);
        message.appendUInt(userId);
        
        String userName = game != null && game.getClientManager() != null ? 
                         game.getClientManager().getNameById(userId) : "";
        message.appendStringWithBreak(userName != null ? userName : "");
        
        message.appendInt32(visits.size());
        
        for (Map<String, Object> visit : visits) {
            long roomId = ((Number) visit.get("room_id")).longValue();
            com.uber.server.game.rooms.RoomData roomData = game != null && game.getRoomManager() != null ?
                                                     game.getRoomManager().generateNullableRoomData(roomId) : null;
            
            if (roomData != null) {
                message.appendBoolean(roomData.isPublicRoom());
                message.appendUInt(roomId);
                message.appendStringWithBreak(roomData.getName());
                message.appendInt32(((Number) visit.get("hour")).intValue());
                message.appendInt32(((Number) visit.get("minute")).intValue());
            }
        }
        
        return message;
    }
    
    /**
     * Serializes user chat log.
     * @param userId User ID
     * @return ServerMessage with user chat log (ID 536)
     */
    public ServerMessage serializeUserChatlog(long userId) {
        if (chatLogRepository == null) {
            return null;
        }
        
        List<Map<String, Object>> visits = chatLogRepository.getUserRoomVisitsWithChatLogs(userId, 5);
        
        ServerMessage message = new ServerMessage(536);
        message.appendUInt(userId);
        
        String userName = game != null && game.getClientManager() != null ?
                         game.getClientManager().getNameById(userId) : "";
        message.appendStringWithBreak(userName != null ? userName : "");
        
        message.appendInt32(visits.size());
        
        for (Map<String, Object> visit : visits) {
            long roomId = ((Number) visit.get("room_id")).longValue();
            com.uber.server.game.rooms.RoomData roomData = game != null && game.getRoomManager() != null ?
                                                     game.getRoomManager().generateNullableRoomData(roomId) : null;
            
            if (roomData == null) {
                continue;
            }
            
            message.appendBoolean(roomData.isPublicRoom());
            message.appendUInt(roomId);
            message.appendStringWithBreak(roomData.getName());
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> logs = (List<Map<String, Object>>) visit.get("chat_logs");
            if (logs != null) {
                message.appendInt32(logs.size());
                for (Map<String, Object> log : logs) {
                    message.appendInt32(((Number) log.get("hour")).intValue());
                    message.appendInt32(((Number) log.get("minute")).intValue());
                    message.appendUInt(((Number) log.get("user_id")).longValue());
                    message.appendStringWithBreak((String) log.get("user_name"));
                    message.appendStringWithBreak((String) log.get("message"));
                }
            } else {
                message.appendInt32(0);
            }
        }
        
        return message;
    }
    
    /**
     * Serializes room chat log.
     * @param roomId Room ID
     * @return ServerMessage with room chat log (ID 535)
     */
    public ServerMessage serializeRoomChatlog(long roomId) {
        com.uber.server.game.rooms.Room room = game != null && game.getRoomManager() != null ?
                                         game.getRoomManager().getRoom(roomId) : null;
        
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomId);
        }
        
        boolean isPublic = room.getData().isPublicRoom();
        
        List<Map<String, Object>> logs = chatLogRepository != null ?
                                        chatLogRepository.getRoomChatLogs(roomId, 150) : new ArrayList<>();
        
        ServerMessage message = new ServerMessage(535);
        message.appendBoolean(isPublic);
        message.appendUInt(roomId);
        message.appendStringWithBreak(room.getData().getName() != null ? room.getData().getName() : "");
        
        message.appendInt32(logs.size());
        for (Map<String, Object> log : logs) {
            message.appendInt32(((Number) log.get("hour")).intValue());
            message.appendInt32(((Number) log.get("minute")).intValue());
            message.appendUInt(((Number) log.get("user_id")).longValue());
            message.appendStringWithBreak((String) log.get("user_name"));
            message.appendStringWithBreak((String) log.get("message"));
        }
        
        return message;
    }
    
    /**
     * Serializes ticket chat log.
     * @param ticket SupportTicket
     * @param roomData RoomData
     * @param timestamp Timestamp
     * @return ServerMessage with ticket chat log (ID 534)
     */
    public ServerMessage serializeTicketChatlog(SupportTicket ticket, com.uber.server.game.rooms.RoomData roomData, long timestamp) {
        if (ticket == null || roomData == null || chatLogRepository == null) {
            return null;
        }
        
        long startTimestamp = timestamp - 300; // 5 minutes before ticket
        long endTimestamp = timestamp;
        
        List<Map<String, Object>> logs = chatLogRepository.getRoomChatLogsInTimeRange(roomData.getId(), startTimestamp, endTimestamp);
        
        ServerMessage message = new ServerMessage(534);
        message.appendUInt(ticket.getId());
        message.appendUInt(ticket.getSenderId());
        message.appendUInt(ticket.getReportedId());
        message.appendBoolean(roomData.isPublicRoom());
        message.appendUInt(roomData.getId());
        message.appendStringWithBreak(roomData.getName());
        
        message.appendInt32(logs.size());
        for (Map<String, Object> log : logs) {
            message.appendInt32(((Number) log.get("hour")).intValue());
            message.appendInt32(((Number) log.get("minute")).intValue());
            message.appendUInt(((Number) log.get("user_id")).longValue());
            message.appendStringWithBreak((String) log.get("user_name"));
            message.appendStringWithBreak((String) log.get("message"));
        }
        
        return message;
    }
    
    /**
     * Serializes room tool for moderation.
     * @param roomData RoomData
     * @return ServerMessage with room tool data (ID 538)
     */
    public ServerMessage serializeRoomTool(com.uber.server.game.rooms.RoomData roomData) {
        if (roomData == null || game == null || game.getUserRepository() == null) {
            return null;
        }
        
        // Get owner ID
        long ownerId = game.getUserRepository().getUserIdByUsername(roomData.getOwner());
        
        com.uber.server.game.rooms.Room room = game.getRoomManager() != null ?
                                         game.getRoomManager().getRoom(roomData.getId()) : null;
        
        ServerMessage message = new ServerMessage(538);
        message.appendUInt(roomData.getId());
        message.appendInt32(roomData.getUsersNow());
        
        // Check if owner is in room (by username)
        boolean ownerInRoom = false;
        if (room != null && roomData.getOwner() != null && !roomData.getOwner().isEmpty()) {
            com.uber.server.game.rooms.RoomUser ownerUser = room.getRoomUserByHabbo(roomData.getOwner());
            ownerInRoom = (ownerUser != null);
        }
        message.appendBoolean(ownerInRoom);
        
        message.appendUInt(ownerId);
        message.appendStringWithBreak(roomData.getOwner() != null ? roomData.getOwner() : "");
        message.appendUInt(roomData.getId());
        message.appendStringWithBreak(roomData.getName() != null ? roomData.getName() : "");
        message.appendStringWithBreak(roomData.getDescription() != null ? roomData.getDescription() : "");
        message.appendInt32(roomData.getTagCount());
        
        // Serialize tags
        if (roomData.getTags() != null) {
            for (String tag : roomData.getTags()) {
                message.appendStringWithBreak(tag != null ? tag : "");
            }
        }
        
        // Serialize event if exists
        if (room != null && room.hasOngoingEvent()) {
            com.uber.server.game.rooms.RoomEvent event = room.getEvent();
            if (event != null) {
                message.appendBoolean(true);
                message.appendStringWithBreak(event.getName() != null ? event.getName() : "");
                message.appendStringWithBreak(event.getDescription() != null ? event.getDescription() : "");
                message.appendInt32(event.getTags() != null ? event.getTags().size() : 0);
                if (event.getTags() != null) {
                    for (String tag : event.getTags()) {
                        message.appendStringWithBreak(tag != null ? tag : "");
                    }
                }
            } else {
                message.appendBoolean(false);
            }
        } else {
            message.appendBoolean(false);
        }
        
        return message;
    }
    
    /**
     * Sends a room alert (caution or message) to all users in a room.
     * @param roomId Room ID
     * @param caution If true, send as caution (increments caution count)
     * @param alertMessage Alert message
     */
    public void roomAlert(long roomId, boolean caution, String alertMessage) {
        if (alertMessage == null || alertMessage.length() <= 1 || game == null || game.getRoomManager() == null) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(roomId);
        if (room == null) {
            return;
        }
        
        List<Long> userIdsToCaution = new ArrayList<>();
        
        // Send alert to all users in room
        for (com.uber.server.game.rooms.RoomUser user : room.getUsers().values()) {
            if (user.isBot()) {
                continue;
            }
            
            GameClient client = user.getClient();
            if (client != null && client.getHabbo() != null) {
                client.sendNotif(alertMessage, caution);
                
                if (caution) {
                    userIdsToCaution.add(client.getHabbo().getId());
                }
            }
        }
        
        // Increment caution counts if applicable
        if (caution && userInfoRepository != null) {
            for (Long userId : userIdsToCaution) {
                userInfoRepository.incrementCaution(userId);
            }
        }
    }
    
    /**
     * Performs a room action (kick users, lock room, mark inappropriate).
     * @param modSession Moderator session
     * @param roomId Room ID
     * @param kickUsers If true, kick all users from room
     * @param lockRoom If true, lock the room (set state to locked)
     * @param inappropriateRoom If true, mark room as inappropriate
     */
    public void performRoomAction(GameClient modSession, long roomId, boolean kickUsers, boolean lockRoom, boolean inappropriateRoom) {
        if (modSession == null || game == null || game.getRoomManager() == null) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(roomId);
        if (room == null) {
            return;
        }
        
        // Lock room
        if (lockRoom) {
            room.getData().setState(1); // Locked
            if (game != null && game.getRoomRepository() != null) {
                game.getRoomRepository().updateRoomState(roomId, 1);
            }
        }
        
        // Mark as inappropriate
        if (inappropriateRoom) {
            room.getData().setName("Inappropriate to Hotel Management");
            room.getData().setDescription("Inappropriate to Hotel Management");
            room.getData().getTags().clear();
            
            if (game != null && game.getRoomRepository() != null) {
                game.getRoomRepository().updateRoomInappropriate(roomId);
            }
        }
        
        // Kick users
        if (kickUsers) {
            List<GameClient> toRemove = new ArrayList<>();
            long modRank = modSession.getHabbo().getRank();
            
            for (com.uber.server.game.rooms.RoomUser user : room.getUsers().values()) {
                if (user.isBot()) {
                    continue;
                }
                
                GameClient client = user.getClient();
                if (client != null && client.getHabbo() != null && client.getHabbo().getRank() < modRank) {
                    toRemove.add(client);
                }
            }
            
            for (GameClient client : toRemove) {
                room.removeUserFromRoom(client, true, false);
            }
        }
    }
    
    /**
     * Alerts a user (sends message or caution).
     * @param modSession Moderator session
     * @param userId User ID to alert
     * @param alertMessage Alert message
     * @param caution If true, send as caution (increments caution count)
     */
    public void alertUser(GameClient modSession, long userId, String alertMessage, boolean caution) {
        if (modSession == null || game == null || game.getClientManager() == null) {
            return;
        }
        
        GameClient client = game.getClientManager().getClientByHabbo(userId);
        if (client == null || client.getHabbo() == null || client.getHabbo().getId() == modSession.getHabbo().getId()) {
            return;
        }
        
        // Check rank for cautions
        if (caution && client.getHabbo().getRank() >= modSession.getHabbo().getRank()) {
            modSession.sendNotif("You do not have permission to caution that user, sending as a regular message instead.");
            caution = false;
        }
        
        client.sendNotif(alertMessage, caution);
        
        // Increment caution count if applicable
        if (caution && userInfoRepository != null) {
            userInfoRepository.incrementCaution(userId);
        }
    }
    
    /**
     * Kicks a user from their current room.
     * @param modSession Moderator session
     * @param userId User ID to kick
     * @param message Kick message
     * @param soft If true, soft kick (no message or caution)
     */
    public void kickUser(GameClient modSession, long userId, String message, boolean soft) {
        if (modSession == null || game == null || game.getClientManager() == null || game.getRoomManager() == null) {
            return;
        }
        
        GameClient client = game.getClientManager().getClientByHabbo(userId);
        if (client == null || client.getHabbo() == null || client.getHabbo().getCurrentRoomId() <= 0 ||
            client.getHabbo().getId() == modSession.getHabbo().getId()) {
            return;
        }
        
        // Check rank
        if (client.getHabbo().getRank() >= modSession.getHabbo().getRank()) {
            modSession.sendNotif("You do not have permission to kick that user.");
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(client.getHabbo().getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        // Remove user from room
        room.removeUserFromRoom(client, true, false);
        
        // Send message and increment caution if not soft
        if (!soft) {
            if (message != null && !message.isEmpty()) {
                client.sendNotif(message);
            }
            
            if (userInfoRepository != null) {
                userInfoRepository.incrementCaution(userId);
            }
        }
    }
    
    /**
     * Bans a user.
     * @param modSession Moderator session
     * @param userId User ID to ban
     * @param length Ban length in seconds
     * @param message Ban message
     */
    public void banUser(GameClient modSession, long userId, long length, String message) {
        if (modSession == null || game == null || game.getClientManager() == null || game.getBanManager() == null) {
            return;
        }
        
        GameClient client = game.getClientManager().getClientByHabbo(userId);
        if (client == null || client.getHabbo() == null || client.getHabbo().getId() == modSession.getHabbo().getId()) {
            return;
        }
        
        // Check rank
        if (client.getHabbo().getRank() >= modSession.getHabbo().getRank()) {
            modSession.sendNotif("You do not have permission to ban that user.");
            return;
        }
        
        // Ban user via BanManager
        String modUsername = modSession.getHabbo().getUsername();
        game.getBanManager().banUser(client, modUsername, length, message, false);
    }
}
