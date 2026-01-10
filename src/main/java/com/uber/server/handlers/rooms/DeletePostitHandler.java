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
 * Handler for deleting a postit (message ID 85).
 * Ported from Messages/Requests/Rooms.cs DeletePostit()
 */
public class DeletePostitHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeletePostitHandler.class);
    private final Game game;
    
    public DeletePostitHandler(Game game) {
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
        
        long itemId = message.popWiredUInt();
        RoomItem item = room.getItem(itemId);
        
        if (item == null) {
            return;
        }
        
        com.uber.server.items.Item baseItem = item.getBaseItem();
        if (baseItem == null || !"postit".equalsIgnoreCase(baseItem.getInteractionType())) {
            return;
        }
        
        // Remove furniture (only owners can delete postits)
        room.removeFurniture(client, itemId);
    }
}
