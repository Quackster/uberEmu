package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.game.pathfinding.Rotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for looking at a position (message ID 79).
 */
public class LookAtHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(LookAtHandler.class);
    private final Game game;
    
    public LookAtHandler(Game game) {
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
        
        int targetX = message.popWiredInt32();
        int targetY = message.popWiredInt32();
        
        // Don't rotate if already at target position
        if (targetX == roomUser.getX() && targetY == roomUser.getY()) {
            return;
        }
        
        int rotation = Rotation.calculate(roomUser.getX(), roomUser.getY(), targetX, targetY);
        roomUser.setRot(rotation);
    }
}
