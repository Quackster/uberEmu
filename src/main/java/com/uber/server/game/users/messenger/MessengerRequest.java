package com.uber.server.game.users.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.MessengerRepository;

/**
 * Represents a friend request.
 */
public class MessengerRequest {
    private final long requestId;
    private final long toUser;
    private final long fromUser;
    private final Game game;
    private final MessengerRepository messengerRepository;
    
    public MessengerRequest(long requestId, long toUser, long fromUser, Game game, MessengerRepository messengerRepository) {
        this.requestId = requestId;
        this.toUser = toUser;
        this.fromUser = fromUser;
        this.game = game;
        this.messengerRepository = messengerRepository;
    }
    
    /**
     * Gets the request ID (actually returns fromUser ID for protocol compatibility).
     * @return From user ID
     */
    public long getRequestId() {
        return fromUser; // Protocol uses fromUser as request ID
    }
    
    /**
     * Gets the actual database request ID.
     * @return Database request ID
     */
    public long getDatabaseRequestId() {
        return requestId;
    }
    
    public long getTo() {
        return toUser;
    }
    
    public long getFrom() {
        return fromUser;
    }
    
    /**
     * Gets the sender's username.
     * @return Username from online client or database
     */
    public String getSenderUsername() {
        GameClient client = getClient(fromUser);
        if (client != null && client.getHabbo() != null) {
            return client.getHabbo().getUsername();
        }
        
        // Fallback to database
        String username = messengerRepository.getUsername(fromUser);
        return username != null ? username : "";
    }
    
    /**
     * Serializes the request to a ServerMessage.
     * @param message ServerMessage to append to
     */
    public void serialize(ServerMessage message) {
        message.appendUInt(fromUser);
        message.appendStringWithBreak(getSenderUsername());
        message.appendStringWithBreak(String.valueOf(fromUser));
    }
    
    /**
     * Gets the GameClient for a user.
     * @param userId User ID
     * @return GameClient object, or null if not online
     */
    private GameClient getClient(long userId) {
        if (game == null || game.getClientManager() == null) {
            return null;
        }
        return game.getClientManager().getClientByHabbo(userId);
    }
}
