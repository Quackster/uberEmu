package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for RoomTagSearchMessageComposer (ID 438).
 * Processes room tag search requests from the client.
 */
public class RoomTagSearchMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(RoomTagSearchMessageComposerHandler.class);
    private final Game game;
    
    public RoomTagSearchMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        if (game.getNavigator() == null) {
            return;
        }
        
        int junk = message.popWiredInt32(); // Unused
        String searchQuery = message.popFixedString();
        // TODO: Replace with GuestRoomSearchResultEventComposer (ID 451)
        client.sendMessage(game.getNavigator().serializeSearchResults(searchQuery));
    }
}
