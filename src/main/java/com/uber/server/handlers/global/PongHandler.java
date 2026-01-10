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
        // Set PongOK flag to indicate client responded
        // This will be implemented in GameClient class
        logger.debug("Received pong from client {}", client);
    }
}
