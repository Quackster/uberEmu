package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting popular rooms (message ID 430).
 */
public class GetPopularRoomsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetPopularRoomsHandler.class);
    private final Game game;
    
    public GetPopularRoomsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.PopularRoomsSearchEvent event = new com.uber.server.event.packet.navigator.PopularRoomsSearchEvent(client, message);
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
            client.sendMessage(game.getNavigator().serializeRoomListing(client, category));
        } catch (NumberFormatException e) {
            // Default to popular if parsing fails
            client.sendMessage(game.getNavigator().serializeRoomListing(client, -1));
        }
    }
}
