package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for entering an inquired room (message ID 233).
 */
public class EnterInquiredRoomHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(EnterInquiredRoomHandler.class);
    private final Game game;
    
    public EnterInquiredRoomHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.navigator.EnterInquiredRoomEvent event = new com.uber.server.event.packet.navigator.EnterInquiredRoomEvent(client, message, roomId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        roomId = event.getRoomId();
        
        // TODO: Implement room inquiry entry functionality
        logger.debug("EnterInquiredRoom called by user {}", 
                    client.getHabbo() != null ? client.getHabbo().getId() : 0);
    }
}
