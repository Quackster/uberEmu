package com.uber.server.handlers.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Handler for requesting a buddy (message ID 39).
 */
public class RequestBuddyHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestBuddyHandler.class);
    private final Game game;
    
    public RequestBuddyHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        String username = message.popFixedString();
        if (username == null || username.isEmpty()) {
            return;
        }
        
        // Use HabboMessenger to handle the request
        com.uber.server.game.users.messenger.HabboMessenger messenger = habbo.getMessenger();
        if (messenger != null) {
            messenger.requestBuddy(username);
        }
    }
}
