package com.uber.server.messages.incoming.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for RemoveBuddyMessageComposer (ID 40).
 * Processes buddy removal requests from the client.
 */
public class RemoveBuddyMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(RemoveBuddyMessageComposerHandler.class);
    private final Game game;
    
    public RemoveBuddyMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getMessenger() == null) {
            return;
        }
        
        int amount = message.popWiredInt32();
        
        for (int i = 0; i < amount; i++) {
            long buddyId = message.popWiredUInt();
            habbo.getMessenger().destroyFriendship(buddyId);
        }
    }
}
