package com.uber.server.messages.incoming.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for RequestBuddyMessageComposer (ID 39).
 * Processes buddy request messages from the client.
 */
public class RequestBuddyMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestBuddyMessageComposerHandler.class);
    private final Game game;
    
    public RequestBuddyMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String username = message.popFixedString();
        
        com.uber.server.event.packet.messenger.RequestBuddyEvent event = new com.uber.server.event.packet.messenger.RequestBuddyEvent(client, message, username);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        username = event.getUsername();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        if (username == null || username.isEmpty()) {
            return;
        }
        
        // Use HabboMessenger to handle the request
        var messenger = habbo.getMessenger();
        if (messenger != null) {
            messenger.requestBuddy(username);
        }
    }
}
