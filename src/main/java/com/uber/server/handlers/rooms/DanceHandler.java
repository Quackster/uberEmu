package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for dancing (message ID 93).
 * Ported from Messages/Requests/Rooms.cs Dance()
 */
public class DanceHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DanceHandler.class);
    private final Game game;
    
    public DanceHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        com.uber.server.game.rooms.RoomUser roomUser = room.getRoomUserByHabbo(habbo.getId());
        if (roomUser == null) {
            return;
        }
        
        roomUser.unidle();
        
        int danceId = message.popWiredInt32();
        
        // Validate dance ID (0-4, and 2-4 require club membership)
        if (danceId < 0 || danceId > 4 || 
            (!habbo.hasFuse("fuse_use_club_dance") && danceId > 1)) {
            danceId = 0;
        }
        
        // Stop carrying item if dancing
        if (danceId > 0 && roomUser.getCarryItemId() > 0) {
            roomUser.carryItem(0);
        }
        
        roomUser.setDanceId(danceId);
        
        ServerMessage danceMessage = new ServerMessage(480);
        danceMessage.appendInt32(roomUser.getVirtualId());
        danceMessage.appendInt32(danceId);
        room.sendMessage(danceMessage);
    }
}
