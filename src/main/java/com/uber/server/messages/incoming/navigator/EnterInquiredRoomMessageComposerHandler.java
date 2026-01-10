package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for EnterInquiredRoomMessageComposer (ID 233).
 * Processes room inquiry entry requests from the client.
 * Note: Class name inferred from pattern - should be verified against XML.
 */
public class EnterInquiredRoomMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(EnterInquiredRoomMessageComposerHandler.class);
    private final Game game;
    
    public EnterInquiredRoomMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        // TODO: Implement room inquiry entry functionality
        logger.debug("EnterInquiredRoom called by user {}", 
                    client.getHabbo() != null ? client.getHabbo().getId() : 0);
    }
}
