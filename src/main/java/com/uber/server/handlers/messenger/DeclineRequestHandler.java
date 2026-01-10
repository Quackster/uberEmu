package com.uber.server.handlers.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for declining a friend request (message ID 38).
 */
public class DeclineRequestHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeclineRequestHandler.class);
    private final Game game;
    
    public DeclineRequestHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        java.util.List<Long> userIds = new java.util.ArrayList<>();
        int amount = message.popWiredInt32();
        for (int i = 0; i < amount; i++) {
            userIds.add(message.popWiredUInt());
        }
        
        com.uber.server.event.packet.messenger.DeclineBuddyEvent event = new com.uber.server.event.packet.messenger.DeclineBuddyEvent(client, message, userIds);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        userIds = event.getUserIds();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getMessenger() == null) {
            return;
        }
        
        // Process declined requests
        for (long requestId : userIds) {
            habbo.getMessenger().handleRequest(requestId);
        }
    }
}
