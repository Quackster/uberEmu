package com.uber.server.messages.incoming.messenger;

import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for FriendListUpdateMessageComposer (ID 15).
 * Processes friend list update requests from the client.
 */
public class FriendListUpdateMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(FriendListUpdateMessageComposerHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.messenger.FriendListUpdateEvent event = new com.uber.server.event.packet.messenger.FriendListUpdateEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        var habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        var messenger = habbo.getMessenger();
        if (messenger == null) {
            return;
        }
        
        // TODO: Replace with FriendListUpdateEventComposer (ID 13)
        client.sendMessage(messenger.serializeUpdates());
    }
}
