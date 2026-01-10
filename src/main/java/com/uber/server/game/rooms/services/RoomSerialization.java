package com.uber.server.game.rooms.services;

import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomUser;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for room serialization helpers.
 * Handles serializing room data to messages.
 */
public class RoomSerialization {
    private static final Logger logger = LoggerFactory.getLogger(RoomSerialization.class);
    
    private final Room room;
    private final ConcurrentHashMap<Long, RoomUser> users;
    
    public RoomSerialization(Room room, ConcurrentHashMap<Long, RoomUser> users) {
        this.room = room;
        this.users = users;
    }
    
    /**
     * Serializes status updates for users in the room.
     */
    public ServerMessage serializeStatusUpdates(boolean all) {
        List<RoomUser> usersToUpdate = new ArrayList<>();
        
        for (RoomUser user : users.values()) {
            if (!all) {
                if (!user.isUpdateNeeded()) {
                    continue;
                }
                user.setUpdateNeeded(false);
            }
            
            if (!user.isSpectator()) {
                usersToUpdate.add(user);
            }
        }
        
        if (usersToUpdate.isEmpty()) {
            return null;
        }
        
        var composer = new com.uber.server.messages.outgoing.rooms.UserStatusUpdateEventComposer(usersToUpdate);
        return composer.compose();
    }
}
