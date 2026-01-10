package com.uber.server.messages.incoming.messenger;

import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for MessengerInitMessageComposer (ID 12).
 * Processes messenger initialization requests from the client.
 */
public class MessengerInitMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessengerInitMessageComposerHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.messenger.MessengerInitEvent event = new com.uber.server.event.packet.messenger.MessengerInitEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        var habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        // Initialize messenger (creates if needed, loads buddies and requests)
        habbo.initMessenger();
    }
}
