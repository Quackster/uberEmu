package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomModel;
import com.uber.server.game.rooms.RoomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for GetRoomData3 (message ID 126).
 * Ported from Messages/Requests/Rooms.cs GetRoomData3()
 * This completes the room entry sequence and adds the user to the room.
 */
public class GetRoomData3Handler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetRoomData3Handler.class);
    private final Game game;
    
    public GetRoomData3Handler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getLoadingRoom() <= 0 || !habbo.isLoadingChecksPassed()) {
            return;
        }
        
        long roomId = habbo.getLoadingRoom();
        
        // Clear loading state
        habbo.setLoadingRoom(0);
        habbo.setLoadingChecksPassed(false);
        
        Room room = game.getRoomManager().getRoom(roomId);
        if (room == null) {
            logger.warn("Room {} not found for user {}", roomId, habbo.getUsername());
            return;
        }
        
        RoomModel model = room.getModel();
        if (model == null) {
            logger.warn("Room model not found for room {}", roomId);
            return;
        }
        
        // Send static furni map (ID 30)
        ServerMessage staticFurniMsg = new ServerMessage(30);
        String staticFurniMap = model.getPublicItems();
        if (staticFurniMap != null && !staticFurniMap.isEmpty()) {
            staticFurniMsg.appendStringWithBreak(staticFurniMap);
        } else {
            staticFurniMsg.appendInt32(0);
        }
        client.sendMessage(staticFurniMsg);
        
        // Send floor and wall items if private room
        if (!room.isPublicRoom()) {
            List<RoomItem> floorItems = room.getFloorItems();
            List<RoomItem> wallItems = room.getWallItems();
            
            // Send floor items (ID 32)
            ServerMessage floorItemsMsg = new ServerMessage(32);
            floorItemsMsg.appendInt32(floorItems.size());
            for (RoomItem item : floorItems) {
                item.serialize(floorItemsMsg);
            }
            client.sendMessage(floorItemsMsg);
            
            // Send wall items (ID 45)
            ServerMessage wallItemsMsg = new ServerMessage(45);
            wallItemsMsg.appendInt32(wallItems.size());
            for (RoomItem item : wallItems) {
                item.serialize(wallItemsMsg);
            }
            client.sendMessage(wallItemsMsg);
        }
        
        // Add user to room
        room.addUserToRoom(client, habbo.isSpectatorMode());
        
        // Send users in room (ID 28)
        List<RoomUser> usersToDisplay = new ArrayList<>();
        for (RoomUser user : room.getUsers().values()) {
            if (!user.isSpectator()) {
                usersToDisplay.add(user);
            }
        }
        
        ServerMessage usersMsg = new ServerMessage(28);
        usersMsg.appendInt32(usersToDisplay.size());
        for (RoomUser user : usersToDisplay) {
            user.serialize(usersMsg);
        }
        client.sendMessage(usersMsg);
        
        // Send room info (ID 471)
        ServerMessage roomInfoMsg = new ServerMessage(471);
        if (room.isPublicRoom()) {
            roomInfoMsg.appendBoolean(false);
            roomInfoMsg.appendStringWithBreak(room.getData().getModelName());
            roomInfoMsg.appendBoolean(false);
        } else {
            roomInfoMsg.appendBoolean(true);
            roomInfoMsg.appendUInt(room.getRoomId());
            roomInfoMsg.appendBoolean(room.checkRights(client, true));
        }
        client.sendMessage(roomInfoMsg);
        
        // Send room data (ID 454) for private rooms
        if (!room.isPublicRoom()) {
            ServerMessage roomDataMsg = new ServerMessage(454);
            roomDataMsg.appendInt32(1);
            roomDataMsg.appendUInt(room.getRoomId());
            roomDataMsg.appendInt32(0);
            roomDataMsg.appendStringWithBreak(room.getData().getName());
            roomDataMsg.appendStringWithBreak(room.getData().getOwner());
            roomDataMsg.appendInt32(room.getData().getState());
            roomDataMsg.appendInt32(0); // Room type specific value (0 = normal room)
            roomDataMsg.appendInt32(room.getData().getUsersMax());
            roomDataMsg.appendStringWithBreak(room.getData().getDescription());
            roomDataMsg.appendInt32(0); // Score display (0 = disabled)
            roomDataMsg.appendInt32(1); // Category display (1 = enabled)
            roomDataMsg.appendInt32(8228); // Category icon (default icon)
            roomDataMsg.appendInt32(room.getData().getCategory());
            roomDataMsg.appendStringWithBreak(""); // Event info (empty = no event)
            roomDataMsg.appendInt32(room.getData().getTags().size());
            for (String tag : room.getData().getTags()) {
                roomDataMsg.appendStringWithBreak(tag);
            }
            client.sendMessage(roomDataMsg);
            
            // Send room event if exists
            if (room.hasOngoingEvent()) {
                client.sendMessage(room.getEvent().serialize(client));
            } else {
                ServerMessage noEventMsg = new ServerMessage(370);
                noEventMsg.appendStringWithBreak("-1");
                client.sendMessage(noEventMsg);
            }
        }
        
        // Send room entry complete
        logger.debug("User {} entered room {}", habbo.getUsername(), roomId);
    }
}
