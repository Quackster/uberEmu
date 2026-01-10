package com.uber.server.handlers.messenger;

import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for initializing messenger (message ID 12).
 */
public class InitMessengerHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(InitMessengerHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.messenger.MessengerInitEvent event = new com.uber.server.event.packet.messenger.MessengerInitEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        com.uber.server.game.Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        // Initialize messenger (creates if needed, loads buddies and requests)
        habbo.initMessenger();
    }
}
