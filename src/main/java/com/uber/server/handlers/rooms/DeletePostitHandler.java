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
 * Handler for deleting a postit (message ID 85).
 */
public class DeletePostitHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeletePostitHandler.class);
    private final Game game;
    
    public DeletePostitHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long itemId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.DeletePostitEvent event = new com.uber.server.event.packet.room.DeletePostitEvent(client, message, itemId);
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
        
        com.uber.server.game.items.Item baseItem = item.getBaseItem();
        if (baseItem == null || !"postit".equalsIgnoreCase(baseItem.getInteractionType())) {
            return;
        }
        
        // Remove furniture (only owners can delete postits)
        room.removeFurniture(client, itemId);
    }
}
