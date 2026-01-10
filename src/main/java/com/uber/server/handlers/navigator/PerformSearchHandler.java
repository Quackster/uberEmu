package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for performing a search (message ID 437).
 */
public class PerformSearchHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(PerformSearchHandler.class);
    private final Game game;
    
    public PerformSearchHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        if (game.getNavigator() == null) {
            return;
        }
        
        String searchQuery = message.popFixedString();
        client.sendMessage(game.getNavigator().serializeSearchResults(searchQuery));
    }
}
