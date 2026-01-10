package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.users.inventory.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for PlaceItemMessageComposer (ID 90).
 * Processes item placement requests from the client.
 */
public class PlaceItemMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(PlaceItemMessageComposerHandler.class);
    private final Game game;
    
    public PlaceItemMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: Handler reads placementData string and parses it, but PlaceItemEvent expects itemId, x, y, rotation directly
        // Using GenericPacketEvent due to parsing structure mismatch
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 90);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client)) {
            return;
        }
        
        // Re-read after event (GenericPacketEvent doesn't store fields)
        message.resetPointer();
        String placementData = message.popFixedString();
        if (placementData == null || placementData.isEmpty()) {
            return;
        }
        
        String[] dataBits = placementData.split(" ");
        if (dataBits.length < 2) {
            return;
        }
        
        try {
            long itemId = Long.parseLong(dataBits[0]);
            UserItem userItem = habbo.getInventoryComponent().getItem(itemId);
            
            if (userItem == null) {
                return;
            }
            
            // Check for dimmer limit
            if ("dimmer".equalsIgnoreCase(userItem.getBaseItem().getInteractionType())) {
                if (room.itemCountByType("dimmer") >= 1) {
                    client.sendNotif("You can only have one moodlight in a room.");
                    return;
                }
            }
            
            // Wall Item
            if (dataBits[1].startsWith(":")) {
                String wallPos = room.wallPositionCheck(":" + placementData.split(":")[1]);
                
                if (wallPos == null) {
                    var errorComposer = new com.uber.server.messages.outgoing.rooms.PlaceObjectErrorComposer(11);
                    client.sendMessage(errorComposer.compose());
                    return;
                }
                
                RoomItem roomItem = new RoomItem(userItem.getId(), room.getRoomId(), 
                                                userItem.getBaseItemId(), 
                                                userItem.getExtraData(), 0, 0, 0.0, 0, wallPos, game);
                
                if (room.setWallItem(client, roomItem)) {
                    habbo.getInventoryComponent().removeItem(itemId);
                }
            }
            // Floor Item
            else {
                if (dataBits.length < 4) {
                    return;
                }
                
                int x = Integer.parseInt(dataBits[1]);
                int y = Integer.parseInt(dataBits[2]);
                int rot = Integer.parseInt(dataBits[3]);
                
                RoomItem roomItem = new RoomItem(userItem.getId(), room.getRoomId(),
                                                userItem.getBaseItemId(),
                                                userItem.getExtraData(), 0, 0, 0, 0, "", game);
                
                if (room.setFloorItem(client, roomItem, x, y, rot, true)) {
                    habbo.getInventoryComponent().removeItem(itemId);
                }
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid placement data format: {}", placementData);
        } catch (Exception e) {
            logger.error("Error placing item: {}", e.getMessage(), e);
        }
    }
}
