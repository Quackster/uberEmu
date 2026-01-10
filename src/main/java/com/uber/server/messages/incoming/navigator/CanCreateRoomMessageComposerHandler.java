package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for CanCreateRoomMessageComposer (ID 387).
 * Processes room creation permission check requests from the client.
 */
public class CanCreateRoomMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(CanCreateRoomMessageComposerHandler.class);
    private final Game game;
    
    public CanCreateRoomMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.CanCreateRoomEvent event = new com.uber.server.event.packet.navigator.CanCreateRoomEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // TODO: Implement room limit check when room limit system is added
        // For now, always allow (return false = no error, 99999 = unlimited)
        var composer = new com.uber.server.messages.outgoing.navigator.CanCreateRoomComposer(false, 99999);
        client.sendMessage(composer.compose());
    }
}
