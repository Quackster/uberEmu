package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting high rated rooms (message ID 431).
 */
public class GetHighRatedRoomsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetHighRatedRoomsHandler.class);
    private final Game game;
    
    public GetHighRatedRoomsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.RoomsWithHighestScoreSearchEvent event = new com.uber.server.event.packet.navigator.RoomsWithHighestScoreSearchEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (game.getNavigator() == null) {
            return;
        }
        
        // Mode -2 = high rated
        client.sendMessage(game.getNavigator().serializeRoomListing(client, -2));
    }
}
