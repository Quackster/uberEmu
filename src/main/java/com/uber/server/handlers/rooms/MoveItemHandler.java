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
 * Handler for moving an item in a room (message ID 73).
 */
public class MoveItemHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MoveItemHandler.class);
    private final Game game;
    
    public MoveItemHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long itemId = message.popWiredUInt();
        int x = message.popWiredInt32();
        int y = message.popWiredInt32();
        int rot = message.popWiredInt32();
        message.popWiredInt32(); // Junk/unused
        
        com.uber.server.event.packet.room.MoveItemEvent event = new com.uber.server.event.packet.room.MoveItemEvent(
            client, message, itemId, x, y, rot);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        itemId = event.getItemId();
        x = event.getX();
        y = event.getY();
        rot = event.getRotation();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client)) {
            return;
        }
        
        RoomItem item = room.getItem(itemId);
        if (item == null) {
            return;
        }
        
        room.setFloorItem(client, item, x, y, rot, false);
    }
}
