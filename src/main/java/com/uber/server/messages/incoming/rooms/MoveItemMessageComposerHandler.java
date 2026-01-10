package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for MoveItemMessageComposer (ID 73).
 * Processes item movement requests from the client.
 */
public class MoveItemMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MoveItemMessageComposerHandler.class);
    private final Game game;
    
    public MoveItemMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long itemId = message.popWiredUInt();
        int x = message.popWiredInt32();
        int y = message.popWiredInt32();
        int rot = message.popWiredInt32();
        int junk = message.popWiredInt32(); // Unused
        
        com.uber.server.event.packet.room.MoveItemEvent event = new com.uber.server.event.packet.room.MoveItemEvent(client, message, itemId, x, y, rot);
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
