package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting favorite rooms (message ID 435).
 */
public class GetFavoriteRoomsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetFavoriteRoomsHandler.class);
    private final Game game;
    
    public GetFavoriteRoomsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.MyFavouriteRoomsSearchEvent event = new com.uber.server.event.packet.navigator.MyFavouriteRoomsSearchEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (game.getNavigator() == null) {
            return;
        }
        
        client.sendMessage(game.getNavigator().serializeFavoriteRooms(client));
    }
}
