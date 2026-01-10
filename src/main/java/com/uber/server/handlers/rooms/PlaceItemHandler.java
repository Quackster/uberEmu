package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.users.inventory.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for placing an item in a room (message ID 90).
 */
public class PlaceItemHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(PlaceItemHandler.class);
    private final Game game;
    
    public PlaceItemHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long itemId = message.popWiredUInt();
        int x = message.popWiredInt32();
        int y = message.popWiredInt32();
        int rotation = message.popWiredInt32();
        
        com.uber.server.event.packet.room.PlaceItemEvent event = new com.uber.server.event.packet.room.PlaceItemEvent(client, message, itemId, x, y, rotation);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        itemId = event.getItemId();
        x = event.getX();
        y = event.getY();
        rotation = event.getRotation();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client)) {
            return;
        }
        
        try {
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
            
            // Floor Item (event structure supports floor items with x, y, rotation)
            RoomItem roomItem = new RoomItem(userItem.getId(), room.getRoomId(),
                                            userItem.getBaseItemId(),
                                            userItem.getExtraData(), 0, 0, 0, 0, "", game);
            
            if (room.setFloorItem(client, roomItem, x, y, rotation, true)) {
                habbo.getInventoryComponent().removeItem(itemId);
            }
        } catch (Exception e) {
            logger.error("Error placing item: {}", e.getMessage(), e);
        }
    }
}
