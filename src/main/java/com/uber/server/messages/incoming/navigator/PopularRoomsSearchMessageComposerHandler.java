package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for PopularRoomsSearchMessageComposer (ID 430).
 * Processes popular rooms search requests from the client.
 */
public class PopularRoomsSearchMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(PopularRoomsSearchMessageComposerHandler.class);
    private final Game game;
    
    public PopularRoomsSearchMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.PopularRoomsSearchEvent event = new com.uber.server.event.packet.navigator.PopularRoomsSearchEvent(
            client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (game.getNavigator() == null) {
            return;
        }
        
        try {
            String categoryStr = message.popFixedString();
            int category = Integer.parseInt(categoryStr);
            // TODO: Replace with GuestRoomSearchResultEventComposer (ID 451)
            client.sendMessage(game.getNavigator().serializeRoomListing(client, category));
        } catch (NumberFormatException e) {
            // Default to popular if parsing fails
            client.sendMessage(game.getNavigator().serializeRoomListing(client, -1));
        }
    }
}
