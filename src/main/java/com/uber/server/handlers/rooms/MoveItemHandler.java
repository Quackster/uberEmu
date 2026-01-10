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
 * Ported from Messages/Requests/Rooms.cs MoveItem()
 */
public class MoveItemHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MoveItemHandler.class);
    private final Game game;
    
    public MoveItemHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client)) {
            return;
        }
        
        long itemId = message.popWiredUInt();
        RoomItem item = room.getItem(itemId);
        
        if (item == null) {
            return;
        }
        
        int x = message.popWiredInt32();
        int y = message.popWiredInt32();
        int rot = message.popWiredInt32();
        int junk = message.popWiredInt32(); // Unused
        
        room.setFloorItem(client, item, x, y, rot, false);
    }
}
