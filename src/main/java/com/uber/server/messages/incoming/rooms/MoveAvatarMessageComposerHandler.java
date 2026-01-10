package com.uber.server.messages.incoming.rooms;

import com.uber.server.event.packet.room.MoveAvatarEvent;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for MoveAvatarMessageComposer (ID 75).
 * Processes avatar movement requests from the client.
 */
public class MoveAvatarMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MoveAvatarMessageComposerHandler.class);
    private final Game game;
    
    public MoveAvatarMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int moveX = message.popWiredInt32();
        int moveY = message.popWiredInt32();
        
        MoveAvatarEvent event = new MoveAvatarEvent(client, message, moveX, moveY);
        Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        moveX = event.getX();
        moveY = event.getY();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        var room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        var roomUser = room.getRoomUserByHabbo(habbo.getId());
        if (roomUser == null || !roomUser.canWalk()) {
            return;
        }
        
        // Don't move if already at destination
        if (moveX == roomUser.getX() && moveY == roomUser.getY()) {
            return;
        }
        
        roomUser.moveTo(moveX, moveY);
    }
}
