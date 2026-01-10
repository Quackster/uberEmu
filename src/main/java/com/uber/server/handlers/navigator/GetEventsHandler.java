package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting events (message ID 439).
 */
public class GetEventsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetEventsHandler.class);
    private final Game game;
    
    public GetEventsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.LatestEventsSearchEvent event = new com.uber.server.event.packet.navigator.LatestEventsSearchEvent(client, message);
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
            client.sendMessage(game.getNavigator().serializeEventListing(client, category));
        } catch (NumberFormatException e) {
            // Default to all events (category 0)
            client.sendMessage(game.getNavigator().serializeEventListing(client, 0));
        }
    }
}
