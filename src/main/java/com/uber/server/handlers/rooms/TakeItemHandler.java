package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for taking an item from a room (message ID 67).
 */
public class TakeItemHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(TakeItemHandler.class);
    private final Game game;
    
    public TakeItemHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int junk = message.popWiredInt32(); // Unused
        long itemId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.TakeItemEvent event = new com.uber.server.event.packet.room.TakeItemEvent(client, message, itemId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        itemId = event.getItemId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        RoomItem item = room.getItem(itemId);
        if (item == null) {
            return;
        }
        
        // Check if item can be picked up
        String interactionType = item.getBaseItem() != null ? 
                                item.getBaseItem().getInteractionType() : "";
        if ("postit".equalsIgnoreCase(interactionType)) {
            return; // Not allowed to pick up post-its
        }
        
        // Remove from room and add to inventory
        room.removeFurniture(client, itemId);
        habbo.getInventoryComponent().addItem(item.getId(), item.getBaseItemId(), item.getExtraData());
        habbo.getInventoryComponent().updateItems(false);
    }
}
