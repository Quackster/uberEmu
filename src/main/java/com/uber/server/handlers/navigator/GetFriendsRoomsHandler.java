package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting friends' rooms (message ID 432).
 */
public class GetFriendsRoomsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetFriendsRoomsHandler.class);
    private final Game game;
    
    public GetFriendsRoomsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.MyFriendsRoomsSearchEvent event = new com.uber.server.event.packet.navigator.MyFriendsRoomsSearchEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (game.getNavigator() == null) {
            return;
        }
        
        // Mode -4 = friends' rooms
        client.sendMessage(game.getNavigator().serializeRoomListing(client, -4));
    }
}
