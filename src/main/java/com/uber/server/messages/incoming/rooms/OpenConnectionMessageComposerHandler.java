package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomData;
import com.uber.server.game.rooms.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for OpenConnectionMessageComposer (ID 391).
 * Handles both public and private rooms, determined dynamically by room data.
 */
public class OpenConnectionMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(OpenConnectionMessageComposerHandler.class);
    private final Game game;
    
    public OpenConnectionMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // OpenConnectionMessageComposer format: reads roomId, password, junk
        int roomId = message.popWiredInt32();
        String password = message.popFixedString();
        message.popWiredInt32(); // Junk
        
        com.uber.server.event.packet.room.OpenConnectionEvent event = new com.uber.server.event.packet.room.OpenConnectionEvent(client, message, roomId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        roomId = event.getRoomId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        prepareRoomForUser(client, habbo, roomId, password);
    }
    
    /**
     * Prepares room for user entry.
     * Made public so it can be called from FollowBuddyHandler
     */
    public void prepareRoomForUser(GameClient client, Habbo habbo, long roomId, String password) {
        // Clear previous loading
        habbo.setLoadingRoom(0);
        habbo.setLoadingChecksPassed(false);
        
        RoomManager roomManager = game.getRoomManager();
        if (roomManager == null) {
            return;
        }
        
        // Generate room data to check if room exists
        RoomData data = roomManager.generateRoomData(roomId);
        if (data == null) {
            logger.warn("Room {} does not exist", roomId);
            return;
        }
        
        // Room type is determined dynamically from room data, not handler type
        
        // Remove user from current room if in room
        if (habbo.isInRoom()) {
            Room oldRoom = roomManager.getRoom(habbo.getCurrentRoomId());
            if (oldRoom != null) {
                oldRoom.removeUserFromRoom(client, false, false);
            }
        }
        
        // Load room if not loaded
        if (!roomManager.isRoomLoaded(roomId)) {
            roomManager.loadRoom(roomId);
        }
        
        Room room = roomManager.getRoom(roomId);
        if (room == null) {
            logger.warn("Failed to load room {}", roomId);
            return;
        }
        
        // Set loading room
        habbo.setLoadingRoom(roomId);
        
        // Check bans
        if (room.userIsBanned(habbo.getId())) {
            if (!room.hasBanExpired(habbo.getId())) {
                var errorComposer = new com.uber.server.messages.outgoing.rooms.CantConnectMessageEventComposer(4); // Banned
                client.sendMessage(errorComposer.compose());
                var quitComposer = new com.uber.server.messages.outgoing.rooms.QuitMessageEventComposer();
                client.sendMessage(quitComposer.compose());
                habbo.setLoadingRoom(0);
                return;
            }
            room.removeBan(habbo.getId());
        }
        
        // Check user count
        if (room.getUserCount() >= room.getData().getUsersMax()) {
            // Check if user has fuse to enter full rooms
            if (!habbo.hasFuse("fuse_enter_full_rooms")) {
                var errorComposer = new com.uber.server.messages.outgoing.rooms.CantConnectMessageEventComposer(1); // Room full
                client.sendMessage(errorComposer.compose());
                var quitComposer = new com.uber.server.messages.outgoing.rooms.QuitMessageEventComposer();
                client.sendMessage(quitComposer.compose());
                habbo.setLoadingRoom(0);
                return;
            }
        }
        
        // Check room state and password
        if (data.isPublicRoom()) {
            // Check if public room is locked (requires mod)
            if (data.getState() > 0) {
                if (!habbo.hasFuse("fuse_mod")) {
                    client.sendNotif("This public room is accessible to Uber staff only.");
                    var quitComposer = new com.uber.server.messages.outgoing.rooms.QuitMessageEventComposer();
                    client.sendMessage(quitComposer.compose());
                    habbo.setLoadingRoom(0);
                    return;
                }
            }
            
            // Send public room entry
            var entryComposer = new com.uber.server.messages.outgoing.rooms.OpenConnectionMessageEventComposer(
                "/client/public/" + data.getModelName() + "/0");
            client.sendMessage(entryComposer.compose());
        } else {
            // Private room
            // Check rights, password, etc.
            boolean canEnter = room.checkRights(client, true);
            canEnter = canEnter || habbo.hasFuse("fuse_enter_any_room");
            
            if (!canEnter && !habbo.isTeleporting()) {
                if (data.getState() == 1) { // Locked
                    if (room.getUserCount() == 0) {
                        var lockedComposer = new com.uber.server.messages.outgoing.rooms.FlatAccessDeniedComposer();
                        client.sendMessage(lockedComposer.compose());
                    } else {
                        var ringComposer = new com.uber.server.messages.outgoing.rooms.DoorbellComposer("");
                        client.sendMessage(ringComposer.compose());
                        
                        // Ring doorbell to room owners
                        var ringToOwnersComposer = new com.uber.server.messages.outgoing.rooms.DoorbellComposer(habbo.getUsername());
                        room.sendMessageToUsersWithRights(ringToOwnersComposer.compose());
                    }
                    habbo.setLoadingRoom(0);
                    return;
                } else if (data.getState() == 2) { // Password
                    String roomPassword = room.getData().getPassword();
                    if (password == null || !password.equalsIgnoreCase(roomPassword != null ? roomPassword : "")) {
                        var errorComposer = new com.uber.server.messages.outgoing.global.GenericErrorEventComposer(-100002);
                        client.sendMessage(errorComposer.compose());
                        var quitComposer = new com.uber.server.messages.outgoing.rooms.QuitMessageEventComposer();
                        client.sendMessage(quitComposer.compose());
                        habbo.setLoadingRoom(0);
                        return;
                    }
                }
            }
            
            // Send private room entry
            var privateEntryComposer = new com.uber.server.messages.outgoing.rooms.OpenFlatConnectionComposer(
                "/client/private/" + roomId + "/id");
            client.sendMessage(privateEntryComposer.compose());
        }
        
        // Mark loading checks as passed
        habbo.setLoadingChecksPassed(true);
        
        // Load room data for user (triggers GetRoomData1/2/3 sequence)
        loadRoomForUser(client, habbo, room);
    }
    
    /**
     * Loads room data for user (sends room data messages).
     */
    private void loadRoomForUser(GameClient client, Habbo habbo, Room room) {
        if (room == null || !habbo.isLoadingChecksPassed()) {
            return;
        }
        
        RoomData data = room.getData();
        if (data == null) {
            return;
        }
        
        // Send group badges
        var groupBadgesComposer = new com.uber.server.messages.outgoing.rooms.HabboGroupBadgesComposer(
            "IcIrDs43103s19014d5a1dc291574a508bc80a64663e61a00");
        client.sendMessage(groupBadgesComposer.compose());
        
        // Send model name and room ID
        var modelComposer = new com.uber.server.messages.outgoing.rooms.RoomReadyComposer(
            data.getModelName(), room.getRoomId());
        client.sendMessage(modelComposer.compose());
        
        // Send spectator mode check if spectator
        if (habbo.isSpectatorMode()) {
            var spectatorComposer = new com.uber.server.messages.outgoing.rooms.YouAreSpectatorComposer();
            client.sendMessage(spectatorComposer.compose());
        }
        
        // Private room specific packets
        if (!room.isPublicRoom()) {
            // Send wallpaper if not "0.0"
            String wallpaper = data.getWallpaper();
            if (wallpaper != null && !wallpaper.equals("0.0")) {
                var wallpaperComposer = new com.uber.server.messages.outgoing.rooms.RoomPropertyComposer("wallpaper", wallpaper);
                client.sendMessage(wallpaperComposer.compose());
            }
            
            // Send floor if not "0.0"
            String floor = data.getFloor();
            if (floor != null && !floor.equals("0.0")) {
                var floorComposer = new com.uber.server.messages.outgoing.rooms.RoomPropertyComposer("floor", floor);
                client.sendMessage(floorComposer.compose());
            }
            
            // Send landscape - always sent (no check for "0.0")
            var landscapeComposer = new com.uber.server.messages.outgoing.rooms.RoomPropertyComposer(
                "landscape", data.getLandscape() != null ? data.getLandscape() : "0.0");
            client.sendMessage(landscapeComposer.compose());
            
            // Send rights if has rights, owner message if owner
            if (room.checkRights(client, true)) {
                // Owner gets both rights and owner messages
                var rightsComposer = new com.uber.server.messages.outgoing.rooms.RoomRightsLevelMessageEventComposer();
                client.sendMessage(rightsComposer.compose());
                
                var ownerComposer = new com.uber.server.messages.outgoing.rooms.YouAreOwnerComposer();
                client.sendMessage(ownerComposer.compose());
            } else if (room.checkRights(client, false)) {
                // Has rights but not owner - only rights message
                var rightsComposer = new com.uber.server.messages.outgoing.rooms.RoomRightsLevelMessageEventComposer();
                client.sendMessage(rightsComposer.compose());
            }
            
            // Send room score (ID 345)
            int scoreToSend = (habbo.getRatedRooms().contains(room.getRoomId()) || room.checkRights(client, true)) 
                ? data.getScore() : -1;
            var scoreComposer = new com.uber.server.messages.outgoing.rooms.RoomRatingComposer(scoreToSend);
            client.sendMessage(scoreComposer.compose());
            
            // Send event (ID 370) - always send for private rooms
            if (room.hasOngoingEvent()) {
                client.sendMessage(room.getEvent().serialize(client));
            } else {
                var noEventComposer = new com.uber.server.messages.outgoing.rooms.RoomEventComposer();
                client.sendMessage(noEventComposer.compose());
            }
        }
    }
}
