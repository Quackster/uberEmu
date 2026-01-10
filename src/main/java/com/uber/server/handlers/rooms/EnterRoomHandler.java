package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.rooms.Room;
import com.uber.server.rooms.RoomData;
import com.uber.server.rooms.RoomManager;
import com.uber.server.rooms.RoomModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for entering a room (message IDs 2 = OpenPub, 391 = OpenFlat).
 * Ported from Messages/Requests/Rooms.cs OpenPub(), OpenFlat()
 */
public class EnterRoomHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(EnterRoomHandler.class);
    private final Game game;
    private final boolean isPublic;
    
    public EnterRoomHandler(Game game, boolean isPublic) {
        this.game = game;
        this.isPublic = isPublic;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        long roomId;
        String password = "";
        
        if (isPublic) {
            // OpenPub: reads junk, roomId, junk
            message.popWiredInt32(); // Junk
            roomId = message.popWiredUInt();
            message.popWiredInt32(); // Junk2
        } else {
            // OpenFlat: reads roomId, password, junk
            roomId = message.popWiredUInt();
            password = message.popFixedString();
            message.popWiredInt32(); // Junk
        }
        
        prepareRoomForUser(client, habbo, roomId, password);
    }
    
    /**
     * Prepares room for user entry.
     * Ported from Messages/Requests/Rooms.cs PrepareRoomForUser()
     */
    private void prepareRoomForUser(GameClient client, Habbo habbo, long roomId, String password) {
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
        
        // Check room type matches handler type
        if (isPublic && !data.isPublicRoom()) {
            logger.debug("User {} tried to enter private room {} as public", habbo.getUsername(), roomId);
            return;
        }
        if (!isPublic && data.isPublicRoom()) {
            logger.debug("User {} tried to enter public room {} as private", habbo.getUsername(), roomId);
            return;
        }
        
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
                ServerMessage error = new ServerMessage(224);
                error.appendInt32(4); // Banned
                client.sendMessage(error);
                client.sendMessage(new ServerMessage(18)); // Leave
                habbo.setLoadingRoom(0);
                return;
            }
            room.removeBan(habbo.getId());
        }
        
        // Check user count
        if (room.getUserCount() >= room.getData().getUsersMax()) {
            // Check if user has fuse to enter full rooms
            if (!habbo.hasFuse("fuse_enter_full_rooms")) {
                ServerMessage error = new ServerMessage(224);
                error.appendInt32(1); // Room full
                client.sendMessage(error);
                client.sendMessage(new ServerMessage(18)); // Leave
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
                    client.sendMessage(new ServerMessage(18));
                    habbo.setLoadingRoom(0);
                    return;
                }
            }
            
            // Send public room entry message
            ServerMessage entryMsg = new ServerMessage(166);
            entryMsg.appendStringWithBreak("/client/public/" + data.getModelName() + "/0");
            client.sendMessage(entryMsg);
        } else {
            // Private room
            ServerMessage entryMsg = new ServerMessage(19);
            client.sendMessage(entryMsg);
            
            // Check rights, password, etc.
            boolean canEnter = room.checkRights(client, true);
            canEnter = canEnter || habbo.hasFuse("fuse_enter_any_room");
            
            if (!canEnter && !habbo.isTeleporting()) {
                if (data.getState() == 1) { // Locked
                    if (room.getUserCount() == 0) {
                        ServerMessage lockedMsg = new ServerMessage(131);
                        client.sendMessage(lockedMsg);
                    } else {
                        ServerMessage ringMsg = new ServerMessage(91);
                        ringMsg.appendStringWithBreak("");
                        client.sendMessage(ringMsg);
                        
                        // Ring doorbell to room owners
                        ServerMessage ringToOwners = new ServerMessage(91);
                        ringToOwners.appendStringWithBreak(habbo.getUsername());
                        room.sendMessageToUsersWithRights(ringToOwners);
                    }
                    habbo.setLoadingRoom(0);
                    return;
                } else if (data.getState() == 2) { // Password
                    String roomPassword = room.getData().getPassword();
                    if (password == null || !password.equalsIgnoreCase(roomPassword != null ? roomPassword : "")) {
                        ServerMessage error = new ServerMessage(33);
                        error.appendInt32(-100002); // Wrong password
                        client.sendMessage(error);
                        client.sendMessage(new ServerMessage(18));
                        habbo.setLoadingRoom(0);
                        return;
                    }
                }
            }
            
            // Send private room entry message
            ServerMessage privateEntryMsg = new ServerMessage(166);
            privateEntryMsg.appendStringWithBreak("/client/private/" + roomId + "/id");
            client.sendMessage(privateEntryMsg);
        }
        
        // Mark loading checks as passed
        habbo.setLoadingChecksPassed(true);
        
        // Load room data for user (triggers GetRoomData1/2/3 sequence)
        loadRoomForUser(client, habbo, room);
    }
    
    /**
     * Loads room data for user (sends room data messages).
     * Ported from Messages/Requests/Rooms.cs LoadRoomForUser()
     */
    private void loadRoomForUser(GameClient client, Habbo habbo, Room room) {
        if (room == null || !habbo.isLoadingChecksPassed()) {
            return;
        }
        
        // Send GetRoomData1 (empty response)
        ServerMessage data1 = new ServerMessage(297);
        data1.appendInt32(0);
        client.sendMessage(data1);
        
        // Send GetRoomData2 (heightmap)
        RoomData data = room.getData();
        if (data == null) {
            return;
        }
        
        RoomModel model = room.getModel();
        if (model == null) {
            client.sendNotif("Sorry, model data is missing from this room and therefore cannot be loaded.");
            client.sendMessage(new ServerMessage(18));
            habbo.setLoadingRoom(0);
            habbo.setLoadingChecksPassed(false);
            return;
        }
        
        // Send heightmap (ID 31) and relative heightmap (ID 470)
        client.sendMessage(model.serializeHeightmap());
        client.sendMessage(model.serializeRelativeHeightmap());
        
        // GetRoomData3 will be handled by GetRoomData3Handler (separate handler)
        // Client will send GetRoomData3 (ID 126) after receiving heightmaps
    }
}
