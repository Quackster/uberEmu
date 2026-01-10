package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for user movement in room (message ID 75).
 */
public class MoveHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MoveHandler.class);
    private final Game game;
    
    public MoveHandler(Game game) {
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
        if (roomUser == null || !roomUser.canWalk()) {
            return;
        }
        
        int moveX = message.popWiredInt32();
        int moveY = message.popWiredInt32();
        
        // Don't move if already at destination
        if (moveX == roomUser.getX() && moveY == roomUser.getY()) {
            return;
        }
        
        roomUser.moveTo(moveX, moveY);
    }
}
