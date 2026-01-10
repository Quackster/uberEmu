package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting rooms with friends (message ID 433).
 */
public class GetRoomsWithFriendsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetRoomsWithFriendsHandler.class);
    private final Game game;
    
    public GetRoomsWithFriendsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.RoomsWhereMyFriendsAreSearchEvent event = new com.uber.server.event.packet.navigator.RoomsWhereMyFriendsAreSearchEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (game.getNavigator() == null) {
            return;
        }
        
        // Mode -5 = rooms with friends
        client.sendMessage(game.getNavigator().serializeRoomListing(client, -5));
    }
}
