package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting own rooms (message ID 434).
 */
public class GetOwnRoomsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetOwnRoomsHandler.class);
    private final Game game;
    
    public GetOwnRoomsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.MyRoomsSearchEvent event = new com.uber.server.event.packet.navigator.MyRoomsSearchEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (game.getNavigator() == null) {
            return;
        }
        
        // Mode -3 = own rooms
        client.sendMessage(game.getNavigator().serializeRoomListing(client, -3));
    }
}
