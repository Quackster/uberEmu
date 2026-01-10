package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for taking an item from a room (message ID 67).
 * Ported from Messages/Requests/Rooms.cs TakeItem()
 */
public class TakeItemHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(TakeItemHandler.class);
    private final Game game;
    
    public TakeItemHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        int junk = message.popWiredInt32(); // Unused
        long itemId = message.popWiredUInt();
        
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
