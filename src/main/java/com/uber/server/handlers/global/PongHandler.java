package com.uber.server.handlers.global;

import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for ping/pong messages (message ID 196).
 */
public class PongHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(PongHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.global.PongEvent event = new com.uber.server.event.packet.global.PongEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Set PongOK flag to indicate client responded
        client.setPongOK(true);
        logger.debug("Received pong from client {}", client.getClientId());
    }
}
