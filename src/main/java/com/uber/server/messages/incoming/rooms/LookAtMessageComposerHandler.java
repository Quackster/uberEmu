package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.game.pathfinding.Rotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for LookAtMessageComposer (ID 79).
 * Processes look-at position requests from the client.
 */
public class LookAtMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(LookAtMessageComposerHandler.class);
    private final Game game;
    
    public LookAtMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int targetX = message.popWiredInt32();
        int targetY = message.popWiredInt32();
        
        com.uber.server.event.packet.room.LookAtEvent event = new com.uber.server.event.packet.room.LookAtEvent(client, message, targetX, targetY);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        targetX = event.getX();
        targetY = event.getY();
        
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
        
        // Don't rotate if already at target position
        if (targetX == roomUser.getX() && targetY == roomUser.getY()) {
            return;
        }
        
        int rotation = Rotation.calculate(roomUser.getX(), roomUser.getY(), targetX, targetY);
        roomUser.setRot(rotation);
    }
}
