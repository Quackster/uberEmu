package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for MyFavouriteRoomsSearchMessageComposer (ID 435).
 * Processes favorite rooms search requests from the client.
 */
public class MyFavouriteRoomsSearchMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MyFavouriteRoomsSearchMessageComposerHandler.class);
    private final Game game;
    
    public MyFavouriteRoomsSearchMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.MyFavouriteRoomsSearchEvent event = new com.uber.server.event.packet.navigator.MyFavouriteRoomsSearchEvent(
            client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (game.getNavigator() == null) {
            return;
        }
        
        // TODO: Replace with FavouritesEventComposer (ID 458)
        client.sendMessage(game.getNavigator().serializeFavoriteRooms(client));
    }
}
