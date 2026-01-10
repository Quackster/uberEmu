package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomModel;
import com.uber.server.game.rooms.RoomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for GetRoomData3MessageComposer (ID 126).
 * Processes third room data request in room entry sequence - completes room entry.
 */
public class GetRoomData3MessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetRoomData3MessageComposerHandler.class);
    private final Game game;
    
    public GetRoomData3MessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.GetRoomData3Event event = new com.uber.server.event.packet.room.GetRoomData3Event(client, message, roomId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        roomId = event.getRoomId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getLoadingRoom() <= 0 || !habbo.isLoadingChecksPassed()) {
            return;
        }
        
        long roomIdLong = habbo.getLoadingRoom();
        
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
        
        // Send static furni map
        String staticFurniMap = model.getPublicItems();
        var staticFurniComposer = new com.uber.server.messages.outgoing.rooms.PublicRoomObjectsComposer(staticFurniMap);
        client.sendMessage(staticFurniComposer.compose());
        
        // Send floor and wall items if private room
        if (!room.isPublicRoom()) {
            List<RoomItem> floorItems = room.getFloorItems();
            List<RoomItem> wallItems = room.getWallItems();
            
            // Send floor items
            ServerMessage floorItemsMsg = new ServerMessage(32);
            floorItemsMsg.appendInt32(floorItems.size());
            for (RoomItem item : floorItems) {
                item.serialize(floorItemsMsg);
            }
            var floorItemsComposer = new com.uber.server.messages.outgoing.rooms.RoomFloorItemsMessageEventComposer(floorItemsMsg);
            client.sendMessage(floorItemsComposer.compose());
            
            // Send wall items
            ServerMessage wallItemsMsg = new ServerMessage(45);
            wallItemsMsg.appendInt32(wallItems.size());
            for (RoomItem item : wallItems) {
                item.serialize(wallItemsMsg);
            }
            var wallItemsComposer = new com.uber.server.messages.outgoing.rooms.ItemsComposer(wallItemsMsg);
            client.sendMessage(wallItemsComposer.compose());
        }
        
        // Add user to room
        room.addUserToRoom(client, habbo.isSpectatorMode());
        
        // Send users in room
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
        var usersComposer = new com.uber.server.messages.outgoing.rooms.RoomUsersMessageEventComposer(usersMsg);
        client.sendMessage(usersComposer.compose());
        
        // Send room info
        var roomInfoComposer = new com.uber.server.messages.outgoing.rooms.RoomEntryInfoComposer(
            !room.isPublicRoom(),
            room.getData().getModelName(),
            room.getRoomId(),
            room.checkRights(client, true));
        client.sendMessage(roomInfoComposer.compose());
        
        // Send room data for private rooms
        if (!room.isPublicRoom()) {
            ServerMessage roomDataMsg = new ServerMessage(454);
            roomDataMsg.appendInt32(1);
            roomDataMsg.appendUInt(room.getRoomId());
            roomDataMsg.appendInt32(0);
            roomDataMsg.appendStringWithBreak(room.getData().getName());
            roomDataMsg.appendStringWithBreak(room.getData().getOwner());
            roomDataMsg.appendInt32(room.getData().getState());
            roomDataMsg.appendInt32(0); // Room type specific value (0 = normal room)
            roomDataMsg.appendInt32(25); // Users max (hardcoded limit)
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
            room.getData().getIcon().serialize(roomDataMsg);
            roomDataMsg.appendBoolean(false);
            var roomDataComposer = new com.uber.server.messages.outgoing.rooms.GetGuestRoomResultComposer(roomDataMsg);
            // client.sendMessage(roomDataComposer.compose());
            
            // Send room event (ID 370) - always send for private rooms
            if (room.hasOngoingEvent()) {
                client.sendMessage(room.getEvent().serialize(client));
            } else {
                var noEventComposer = new com.uber.server.messages.outgoing.rooms.RoomEventComposer();
                ServerMessage noEventMsg = noEventComposer.compose();
                // client.sendMessage(noEventMsg);
            }
        }
        
        // Send status updates (for all rooms)
        ServerMessage statusUpdates = room.serializeStatusUpdates(true);
        if (statusUpdates != null) {
          //  client.sendMessage(statusUpdates);
        }
        
        // Send individual user status updates (dancing, sleeping, carrying items, effects) - for all rooms
        for (RoomUser user : room.getUsers().values()) {
            if (user.isSpectator()) {
                continue;
            }
            
            // Send dancing status using DanceMessageComposer (ID 480)
            if (user.isDancing()) {
                var danceComposer = new com.uber.server.messages.outgoing.rooms.DanceMessageComposer(
                    user.getVirtualId(), user.getDanceId());
                client.sendMessage(danceComposer.compose());
            }
            
            // Send sleeping status
            if (user.isAsleep()) {
                var sleepComposer = new com.uber.server.messages.outgoing.rooms.SleepComposer(
                    user.getVirtualId(), true);
                client.sendMessage(sleepComposer.compose());
            }
            
            // Send carrying item status
            if (user.getCarryItemId() > 0 && user.getCarryTimer() > 0) {
                var carryComposer = new com.uber.server.messages.outgoing.rooms.UserCarryItemMessageEventComposer(
                    user.getVirtualId(), user.getCarryTimer());
                client.sendMessage(carryComposer.compose());
            }
            
            // Send avatar effect status - only for non-bot users
            if (!user.isBot()) {
                GameClient userClient = user.getClient();
                if (userClient != null && userClient.getHabbo() != null) {
                    com.uber.server.game.Habbo userHabbo = userClient.getHabbo();
                    if (userHabbo.getAvatarEffectsInventoryComponent() != null) {
                        int currentEffect = userHabbo.getAvatarEffectsInventoryComponent().getCurrentEffect();
                        if (currentEffect >= 1) {
                            var effectComposer = new com.uber.server.messages.outgoing.rooms.AvatarEffectComposer(
                                user.getVirtualId(), currentEffect);
                            client.sendMessage(effectComposer.compose());
                        }
                    }
                }
            }
        }
        
        // Send room entry complete
        logger.debug("User {} entered room {}", habbo.getUsername(), roomId);
    }
}
