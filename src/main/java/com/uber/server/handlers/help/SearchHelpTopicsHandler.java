package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for searching help topics (message ID 419).
 * Ported from Messages/Requests/Help.cs SearchHelpTopics()
 */
public class SearchHelpTopicsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SearchHelpTopicsHandler.class);
    private final Game game;
    
    public SearchHelpTopicsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        String searchQuery = message.popFixedString();
        
        if (searchQuery == null || searchQuery.length() < 3) {
            return;
        }
        
        client.sendMessage(game.getHelpTool().serializeSearchResults(searchQuery));
    }
}
