package com.uber.server.handlers.messenger;

import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for friends list update (message ID 15).
 */
public class FriendsListUpdateHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(FriendsListUpdateHandler.class);
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.messenger.FriendListUpdateEvent event = new com.uber.server.event.packet.messenger.FriendListUpdateEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        com.uber.server.game.Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        com.uber.server.game.users.messenger.HabboMessenger messenger = habbo.getMessenger();
        if (messenger == null) {
            return;
        }
        
        client.sendMessage(messenger.serializeUpdates());
    }
}
