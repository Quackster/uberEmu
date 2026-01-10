package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for MyFriendsRoomsSearchMessageComposer (ID 432).
 * Processes friends' rooms search requests from the client.
 */
public class MyFriendsRoomsSearchMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MyFriendsRoomsSearchMessageComposerHandler.class);
    private final Game game;
    
    public MyFriendsRoomsSearchMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.MyFriendsRoomsSearchEvent event = new com.uber.server.event.packet.navigator.MyFriendsRoomsSearchEvent(
            client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (game.getNavigator() == null) {
            return;
        }
        
        // Mode -4 = friends' rooms
        // TODO: Replace with GuestRoomSearchResultEventComposer (ID 451)
        client.sendMessage(game.getNavigator().serializeRoomListing(client, -4));
    }
}
