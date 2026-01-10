package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for RoomTextSearchMessageComposer (ID 437).
 * Processes room text search requests from the client.
 */
public class RoomTextSearchMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(RoomTextSearchMessageComposerHandler.class);
    private final Game game;
    
    public RoomTextSearchMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        if (game.getNavigator() == null) {
            return;
        }
        
        String searchQuery = message.popFixedString();
        // TODO: Replace with GuestRoomSearchResultEventComposer (ID 451)
        client.sendMessage(game.getNavigator().serializeSearchResults(searchQuery));
    }
}
