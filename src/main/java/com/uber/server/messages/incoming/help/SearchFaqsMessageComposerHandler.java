package com.uber.server.messages.incoming.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for SearchFaqsMessageComposer (ID 419).
 * Processes FAQ search requests from the client.
 */
public class SearchFaqsMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(SearchFaqsMessageComposerHandler.class);
    private final Game game;
    
    public SearchFaqsMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        String searchQuery = message.popFixedString();
        
        if (searchQuery == null || searchQuery.length() < 3) {
            return;
        }
        
        // TODO: Replace with FaqSearchResultsMessageEventComposer (ID 521)
        client.sendMessage(game.getHelpTool().serializeSearchResults(searchQuery));
    }
}
