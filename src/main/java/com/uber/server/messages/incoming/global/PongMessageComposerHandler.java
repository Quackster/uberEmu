package com.uber.server.messages.incoming.global;

import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for PongMessageComposer (ID 196).
 * Processes pong responses from the client.
 */
public class PongMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(PongMessageComposerHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        // Set PongOK flag to indicate client responded
        client.setPongOK(true);
        logger.debug("Received pong from client {}", client.getClientId());
    }
}
