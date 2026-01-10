package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for requesting room load for user (message ID 59).
 * 
 * This is a simple handler that triggers the room loading sequence.
 */
public class ReqLoadRoomForUserHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ReqLoadRoomForUserHandler.class);
    private final Game game;
    
    public ReqLoadRoomForUserHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.ReqLoadRoomForUserEvent event = new com.uber.server.event.packet.room.ReqLoadRoomForUserEvent(client, message, userId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        userId = event.getUserId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getLoadingRoom() <= 0) {
            return;
        }
        
        // This handler simply triggers the room loading sequence
        // The actual loading is handled by GetRoomData1/2/3 handlers
        logger.debug("ReqLoadRoomForUser called for user {} loading room {}", 
                    habbo.getId(), habbo.getLoadingRoom());
    }
}
