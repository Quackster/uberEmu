package com.uber.server.handlers.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for removing a buddy (message ID 40).
 */
public class RemoveBuddyHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RemoveBuddyHandler.class);
    private final Game game;
    
    public RemoveBuddyHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        
        com.uber.server.event.packet.messenger.RemoveBuddyEvent event = new com.uber.server.event.packet.messenger.RemoveBuddyEvent(client, message, userId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        userId = event.getUserId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getMessenger() == null) {
            return;
        }
        
        // Note: Handler processes single user, but original code processed multiple
        // Keeping single user processing to match event structure
        habbo.getMessenger().destroyFriendship(userId);
    }
}
