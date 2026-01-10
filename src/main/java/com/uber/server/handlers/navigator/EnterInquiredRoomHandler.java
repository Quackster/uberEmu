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
        // TODO: Implement room inquiry entry functionality
        logger.debug("EnterInquiredRoom called by user {}", 
                    client.getHabbo() != null ? client.getHabbo().getId() : 0);
    }
}
