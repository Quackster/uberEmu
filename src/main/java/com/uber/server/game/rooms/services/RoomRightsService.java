package com.uber.server.game.rooms.services;

import com.uber.server.game.GameClient;
import com.uber.server.game.rooms.Room;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for managing room rights (room managers).
 * Extracted from Room class to improve separation of concerns.
 */
public class RoomRightsService {
    private static final Logger logger = LoggerFactory.getLogger(RoomRightsService.class);
    
    private final Room room;
    private final CopyOnWriteArrayList<Long> usersWithRights;
    
    public RoomRightsService(Room room, List<Long> initialRights) {
        this.room = room;
        this.usersWithRights = new CopyOnWriteArrayList<>(initialRights);
    }
    
    /**
     * Checks if a session has room rights.
     * @param session GameClient session
     * @return True if user has rights
     */
    public boolean checkRights(GameClient session) {
        return checkRights(session, false);
    }
    
    /**
     * Checks if a session has room rights.
     * @param session GameClient session
     * @param requireOwnership If true, requires ownership (not just rights)
     * @return True if user has rights
     */
    public boolean checkRights(GameClient session, boolean requireOwnership) {
        if (session == null || session.getHabbo() == null) {
            return false;
        }
        
        long userId = session.getHabbo().getId();
        
        // Check ownership
        if (room.getOwner().equals(session.getHabbo().getUsername())) {
            return true;
        }
        
        // If ownership is required, return false
        if (requireOwnership) {
            return false;
        }
        
        // Check rights
        return hasRights(userId);
    }
    
    /**
     * Checks if a user has room rights.
     * @param userId User ID
     * @return True if user has rights
     */
    public boolean hasRights(long userId) {
        return usersWithRights.contains(userId);
    }
    
    /**
     * Adds rights for a user.
     * @param userId User ID
     * @return True if rights were added
     */
    public boolean addRight(long userId) {
        if (hasRights(userId)) {
            return false;
        }
        
        usersWithRights.add(userId);
        
        // Send update to room
        ServerMessage update = new ServerMessage(42);
        update.appendUInt(room.getRoomId());
        update.appendUInt(userId);
        update.appendStringWithBreak("");
        room.sendMessage(update);
        
        return true;
    }
    
    /**
     * Removes rights for a user.
     * @param userId User ID
     * @return True if rights were removed
     */
    public boolean removeRight(long userId) {
        boolean removed = usersWithRights.remove(userId);
        
        if (removed) {
            // Send update to room
            ServerMessage update = new ServerMessage(43);
            update.appendUInt(room.getRoomId());
            update.appendUInt(userId);
            room.sendMessage(update);
        }
        
        return removed;
    }
    
    /**
     * Removes all rights.
     * @return Number of rights removed
     */
    public int removeAllRights() {
        int count = usersWithRights.size();
        usersWithRights.clear();
        
        // Send update to room
        ServerMessage update = new ServerMessage(44);
        update.appendUInt(room.getRoomId());
        room.sendMessage(update);
        
        return count;
    }
    
    /**
     * Gets list of users with rights.
     * @return List of user IDs with rights
     */
    public List<Long> getUsersWithRights() {
        return List.copyOf(usersWithRights);
    }
    
    /**
     * Sends a message to all users with rights.
     * @param message Message to send
     */
    public void sendMessageToUsersWithRights(ServerMessage message) {
        if (message == null) {
            return;
        }
        
        for (Long userId : usersWithRights) {
            com.uber.server.game.GameClient client = com.uber.server.game.Game.getInstance()
                    .getClientManager().getClientByHabbo(userId);
            if (client != null && client.getHabbo() != null 
                    && client.getHabbo().getCurrentRoomId() == room.getRoomId()) {
                client.sendMessage(message);
            }
        }
    }
}
