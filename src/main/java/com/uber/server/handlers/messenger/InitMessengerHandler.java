package com.uber.server.handlers.messenger;

import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for initializing messenger (message ID 12).
 * Ported from Messages/Requests/Messenger.cs InitMessenger()
 */
public class InitMessengerHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(InitMessengerHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        com.uber.server.game.Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        com.uber.server.game.users.messenger.HabboMessenger messenger = habbo.getMessenger();
        if (messenger == null) {
            return;
        }
        
        // Send messenger initialization data (buddies and requests)
        client.sendMessage(messenger.serializeUpdates());
    }
}
