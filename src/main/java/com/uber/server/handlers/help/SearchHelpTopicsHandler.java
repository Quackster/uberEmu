package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for searching help topics (message ID 419).
 */
public class SearchHelpTopicsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SearchHelpTopicsHandler.class);
    private final Game game;
    
    public SearchHelpTopicsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String searchQuery = message.popFixedString();
        
        // Note: This handler may be unused - SearchFaqsMessageComposerHandler is registered for ID 419
        com.uber.server.event.packet.help.SearchFaqsEvent event = new com.uber.server.event.packet.help.SearchFaqsEvent(
            client, message, searchQuery);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        searchQuery = event.getSearchQuery();
        
        if (searchQuery == null || searchQuery.length() < 3) {
            return;
        }
        
        client.sendMessage(game.getHelpTool().serializeSearchResults(searchQuery));
    }
}
