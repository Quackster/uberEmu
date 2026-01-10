package com.uber.server.game.users.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.MessengerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a friend/buddy in the messenger system.
 */
public class MessengerBuddy {
    private static final Logger logger = LoggerFactory.getLogger(MessengerBuddy.class);
    
    private final long userId;
    private final Game game;
    private final MessengerRepository messengerRepository;
    private boolean updateNeeded;
    
    public MessengerBuddy(long userId, Game game, MessengerRepository messengerRepository) {
        this.userId = userId;
        this.game = game;
        this.messengerRepository = messengerRepository;
        this.updateNeeded = false;
    }
    
    public long getId() {
        return userId;
    }
    
    public boolean isUpdateNeeded() {
        return updateNeeded;
    }
    
    public void setUpdateNeeded(boolean updateNeeded) {
        this.updateNeeded = updateNeeded;
    }
    
    /**
     * Gets the buddy's username.
     * @return Username from online client or database
     */
    public String getUsername() {
        GameClient client = getClient();
        if (client != null && client.getHabbo() != null) {
            return client.getHabbo().getUsername();
        }
        
        // Fallback to database
        String username = messengerRepository.getUsername(userId);
        return username != null ? username : "";
    }
    
    /**
     * Gets the buddy's real name.
     * @return Real name from online client or database
     */
    public String getRealName() {
        GameClient client = getClient();
        if (client != null && client.getHabbo() != null) {
            return client.getHabbo().getRealName();
        }
        
        // Get from database
        if (game != null && game.getUserRepository() != null) {
            String realName = game.getUserRepository().getRealName(userId);
            return realName != null ? realName : "";
        }
        
        return "";
    }
    
    /**
     * Gets the buddy's look (appearance).
     * @return Look string, or empty string if offline
     */
    public String getLook() {
        GameClient client = getClient();
        if (client != null && client.getHabbo() != null) {
            return client.getHabbo().getLook();
        }
        
        return "";
    }
    
    /**
     * Gets the buddy's motto.
     * @return Motto string, or empty string if offline
     */
    public String getMotto() {
        GameClient client = getClient();
        if (client != null && client.getHabbo() != null) {
            return client.getHabbo().getMotto();
        }
        
        return "";
    }
    
    /**
     * Gets the buddy's last online timestamp.
     * @return Last online string, or empty string if online
     */
    public String getLastOnline() {
        GameClient client = getClient();
        if (client != null) {
            // User is online
            return "";
        }
        
        // Get from database
        if (game != null && game.getUserRepository() != null) {
            long lastOnline = game.getUserRepository().getLastOnline(userId);
            if (lastOnline > 0) {
                // Format as "DD/MM/YYYY HH:MM" or similar
                java.time.Instant instant = java.time.Instant.ofEpochSecond(lastOnline);
                java.time.LocalDateTime dateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
                return String.format("%02d/%02d/%04d %02d:%02d", 
                    dateTime.getDayOfMonth(), dateTime.getMonthValue(), dateTime.getYear(),
                    dateTime.getHour(), dateTime.getMinute());
            }
        }
        
        return "";
    }
    
    /**
     * Checks if the buddy is online.
     * @return True if online and not appearing offline
     */
    public boolean isOnline() {
        GameClient client = getClient();
        if (client == null || client.getHabbo() == null) {
            return false;
        }
        
        HabboMessenger messenger = client.getHabbo().getMessenger();
        if (messenger == null || messenger.isAppearOffline()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if the buddy is in a room.
     * @return True if online and in a room
     */
    public boolean isInRoom() {
        if (!isOnline()) {
            return false;
        }
        
        GameClient client = getClient();
        if (client == null || client.getHabbo() == null) {
            return false;
        }
        
        return client.getHabbo().isInRoom();
    }
    
    /**
     * Serializes the buddy to a ServerMessage.
     * @param message ServerMessage to append to
     * @param search If true, serializes for search results; if false, for friends list
     */
    public void serialize(ServerMessage message, boolean search) {
        if (search) {
            message.appendUInt(userId);
            message.appendStringWithBreak(getUsername());
            message.appendStringWithBreak(getMotto());
            message.appendBoolean(isOnline());
            message.appendBoolean(isInRoom());
            message.appendStringWithBreak("");
            message.appendBoolean(false);
            message.appendStringWithBreak(getLook());
            message.appendStringWithBreak(getLastOnline());
            message.appendStringWithBreak(getRealName());
        } else {
            message.appendUInt(userId);
            message.appendStringWithBreak(getUsername());
            message.appendBoolean(true);
            message.appendBoolean(isOnline());
            message.appendBoolean(isInRoom());
            message.appendStringWithBreak(getLook());
            message.appendBoolean(false);
            message.appendStringWithBreak(getMotto());
            message.appendStringWithBreak(getLastOnline());
            message.appendStringWithBreak(getRealName());
        }
    }
    
    /**
     * Gets the GameClient for this buddy.
     * @return GameClient object, or null if not online
     */
    private GameClient getClient() {
        if (game == null || game.getClientManager() == null) {
            return null;
        }
        return game.getClientManager().getClientByHabbo(userId);
    }
}
