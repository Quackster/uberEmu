package com.uber.server.handlers.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.game.users.messenger.MessengerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for accepting a friend request (message ID 37).
 */
public class AcceptRequestHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(AcceptRequestHandler.class);
    private final Game game;
    
    public AcceptRequestHandler(Game game) {
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
        
        com.uber.server.event.packet.messenger.AcceptBuddyEvent event = new com.uber.server.event.packet.messenger.AcceptBuddyEvent(client, message, userIds);
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
        
        for (long requestId : userIds) {
            
            MessengerRequest request = habbo.getMessenger().getRequest(requestId);
            if (request == null) {
                continue;
            }
            
            // Verify this request is for this user
            if (request.getTo() != habbo.getId()) {
                // Not this user's request - possible exploit attempt
                return;
            }
            
            // Create friendship if it doesn't exist
            if (!habbo.getMessenger().friendshipExists(habbo.getId(), request.getFrom())) {
                habbo.getMessenger().createFriendship(request.getFrom());
            }
            
            // Handle the request (removes it)
            habbo.getMessenger().handleRequest(request.getFrom());
        }
    }
}
