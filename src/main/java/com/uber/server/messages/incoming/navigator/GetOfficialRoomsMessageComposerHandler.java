package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetOfficialRoomsMessageComposer (ID 380).
 * Processes official rooms requests from the client.
 */
public class GetOfficialRoomsMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetOfficialRoomsMessageComposerHandler.class);
    private final Game game;
    
    public GetOfficialRoomsMessageComposerHandler(Game game) {
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
        
        var navigator = game.getNavigator();
        if (navigator == null) {
            return;
        }
        
        // TODO: Replace with OfficialRoomsEventComposer (ID 450)
        client.sendMessage(navigator.serializePublicRooms());
    }
}
