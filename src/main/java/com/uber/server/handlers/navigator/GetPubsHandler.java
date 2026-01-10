package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting public rooms (message ID 380).
 */
public class GetPubsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetPubsHandler.class);
    private final Game game;
    
    public GetPubsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.GetOfficialRoomsEvent event = new com.uber.server.event.packet.navigator.GetOfficialRoomsEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        com.uber.server.game.navigator.Navigator navigator = game.getNavigator();
        if (navigator == null) {
            return;
        }
        
        client.sendMessage(navigator.serializePublicRooms());
    }
}
