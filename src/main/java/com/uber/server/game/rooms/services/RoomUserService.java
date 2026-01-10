package com.uber.server.game.rooms.services;

import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.RoomItem;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomModel;
import com.uber.server.game.rooms.RoomUser;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing users in a room.
 * Handles adding, removing, and querying room users.
 */
public class RoomUserService {
    private static final Logger logger = LoggerFactory.getLogger(RoomUserService.class);
    
    private final Room room;
    private final ConcurrentHashMap<Long, RoomUser> users;
    private final RoomRightsService rightsService;
    private int userCounter;
    
    public RoomUserService(Room room, ConcurrentHashMap<Long, RoomUser> users, 
                          RoomRightsService rightsService, int initialUserCounter) {
        this.room = room;
        this.users = users;
        this.rightsService = rightsService;
        this.userCounter = initialUserCounter;
    }
    
    /**
     * Adds a user to the room.
     */
    public void addUserToRoom(GameClient session, boolean spectator) {
        if (session == null || session.getHabbo() == null) {
            return;
        }
        
        Habbo habbo = session.getHabbo();
        RoomUser user = new RoomUser(habbo.getId(), room.getRoomId(), userCounter++, room.getGame());
        
        if (spectator) {
            user.setSpectator(true);
        } else {
            // Get room model for door position
            RoomModel model = room.getModel();
            if (model != null) {
                user.setPos(model.getDoorX(), model.getDoorY(), model.getDoorZ());
                user.setRot(model.getDoorDir());
            }
            
            // Add status based on rights
            if (rightsService.checkRights(session, true)) {
                user.addStatus("roomControl", "useradmin");
            } else if (rightsService.checkRights(session, false)) {
                user.addStatus("roomControl", "");
            }
            
            // Handle teleporting
            if (habbo.isTeleporting()) {
                RoomItem teleporter = room.getItem(habbo.getTeleporterId());
                if (teleporter != null) {
                    user.setPos(teleporter.getX(), teleporter.getY(), teleporter.getZ());
                    user.setRot(teleporter.getRot());
                    teleporter.setInteractingUser2(habbo.getId());
                    teleporter.setExtraData("2");
                    teleporter.updateState(false, true);
                }
            }
            
            habbo.setTeleporting(false);
            habbo.setTeleporterId(0);
            
            // Send enter message to room
            ServerMessage enterMessage = new ServerMessage(28);
            enterMessage.appendInt32(1);
            user.serialize(enterMessage);
            room.sendMessage(enterMessage);
        }
        
        users.put(habbo.getId(), user);
        
        // Update Habbo's current room
        habbo.setCurrentRoomId(room.getRoomId());
        
        // Call habbo.onEnterRoom()
        habbo.onEnterRoom(room.getRoomId());
        
        if (!spectator) {
            room.updateUserCount();
        }
    }
    
    /**
     * Removes a user from the room.
     */
    public void removeUserFromRoom(GameClient session, boolean notifyClient, boolean notifyKick) {
        if (session == null || session.getHabbo() == null) {
            return;
        }
        
        Habbo habbo = session.getHabbo();
        RoomUser user = users.remove(habbo.getId());
        
        if (user == null) {
            return;
        }
        
        if (notifyClient) {
            if (notifyKick) {
                var kickComposer = new com.uber.server.messages.outgoing.global.GenericErrorComposer(4008);
                ServerMessage kickMessage = kickComposer.compose();
                session.sendMessage(kickMessage);
            }
            
            var leaveComposer = new com.uber.server.messages.outgoing.rooms.RoomEntryErrorMessageEventComposer();
            session.sendMessage(leaveComposer.compose());
        }
        
        if (!user.isSpectator()) {
            // Send leave message to room
            ServerMessage leaveUpdate = new ServerMessage(29);
            leaveUpdate.appendUInt(user.getVirtualId());
            room.sendMessage(leaveUpdate);
            
            // Stop active trades
            if (room.hasActiveTrade(habbo.getId())) {
                room.tryStopTrade(habbo.getId());
            }
            
            // Clear room event if owner left
            if (habbo.getUsername().toLowerCase().equals(room.getData().getOwner().toLowerCase())) {
                if (room.hasOngoingEvent()) {
                    room.setEvent(null);
                    var eventComposer = new com.uber.server.messages.outgoing.rooms.RoomEventComposer();
                    ServerMessage message = eventComposer.compose();
                    room.sendMessage(message);
                }
            }
            
            // Update Habbo's current room
            habbo.setCurrentRoomId(0);
            
            // Call habbo.onLeaveRoom()
            habbo.onLeaveRoom();
            
            room.updateUserCount();
            
            // Notify bots
            for (RoomUser bot : users.values()) {
                if (bot.isBot() && bot.getBotAI() != null) {
                    bot.getBotAI().onUserLeaveRoom(session);
                }
            }
        }
    }
    
    /**
     * Gets a RoomUser by Habbo ID.
     */
    public RoomUser getRoomUserByHabbo(long habboId) {
        for (RoomUser user : users.values()) {
            if (user.isBot() || user.isSpectator()) {
                continue;
            }
            
            if (user.getHabboId() == habboId) {
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * Gets a RoomUser by Habbo username.
     */
    public RoomUser getRoomUserByHabbo(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        
        String lowerUsername = username.toLowerCase();
        for (RoomUser user : users.values()) {
            if (user.isBot() || user.isSpectator()) {
                continue;
            }
            
            GameClient client = user.getClient();
            if (client != null && client.getHabbo() != null) {
                if (client.getHabbo().getUsername().toLowerCase().equals(lowerUsername)) {
                    return user;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Gets a RoomUser by virtual ID.
     */
    public RoomUser getRoomUserByVirtualId(int virtualId) {
        for (RoomUser user : users.values()) {
            if (user.getVirtualId() == virtualId) {
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the current user counter.
     */
    public int getUserCounter() {
        return userCounter;
    }
    
    /**
     * Called when a user says something in the room.
     * Notifies all bots in the room.
     */
    public void onUserSay(RoomUser user, String message, boolean shout) {
        if (user == null || message == null) {
            return;
        }
        
        for (RoomUser roomUser : users.values()) {
            if (!roomUser.isBot() || roomUser.getBotAI() == null) {
                continue;
            }
            
            if (shout) {
                roomUser.getBotAI().onUserShout(user, message);
            } else {
                roomUser.getBotAI().onUserSay(user, message);
            }
        }
    }
    
    /**
     * Makes all users in the room turn their heads to look at a coordinate.
     */
    public void turnHeads(int x, int y, long senderId) {
        for (RoomUser user : users.values()) {
            if (user.getHabboId() == senderId || user.isBot() || user.isSpectator()) {
                continue;
            }
            
            // Calculate rotation to look at the coordinate
            int rot = com.uber.server.game.pathfinding.Rotation.calculate(user.getX(), user.getY(), x, y);
            user.setRot(rot, true); // true = head only
        }
    }
    
    /**
     * Gets user count (excluding bots and spectators).
     */
    public int getUserCount() {
        int count = 0;
        for (RoomUser user : users.values()) {
            if (!user.isBot() && !user.isSpectator()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Gets pet count in room.
     */
    public int getPetCount() {
        int count = 0;
        for (RoomUser user : users.values()) {
            if (user.isPet()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Gets a pet by pet ID.
     */
    public RoomUser getPet(long petId) {
        for (RoomUser user : users.values()) {
            if (user.isPet() && user.getPetData() != null && user.getPetData().getPetId() == petId) {
                return user;
            }
        }
        return null;
    }
}
