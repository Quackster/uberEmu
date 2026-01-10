package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for MyRoomsSearchMessageComposer (ID 434).
 * Processes own rooms search requests from the client.
 */
public class MyRoomsSearchMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MyRoomsSearchMessageComposerHandler.class);
    private final Game game;
    
    public MyRoomsSearchMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.MyRoomsSearchEvent event = new com.uber.server.event.packet.navigator.MyRoomsSearchEvent(
            client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (game.getNavigator() == null) {
            return;
        }
        
        // Mode -3 = own rooms
        // TODO: Replace with GuestRoomSearchResultEventComposer (ID 451)
        client.sendMessage(game.getNavigator().serializeRoomListing(client, -3));
    }
}
