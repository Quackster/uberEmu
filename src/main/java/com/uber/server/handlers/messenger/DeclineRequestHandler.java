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
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getMessenger() == null) {
            return;
        }
        
        // Mode: 0 = decline specific, 1 = decline all
        int mode = message.popWiredInt32();
        int amount = message.popWiredInt32();
        
        if (mode == 0 && amount == 1) {
            // Decline specific request
            long requestId = message.popWiredUInt(); // Actually fromUser ID
            habbo.getMessenger().handleRequest(requestId);
        } else if (mode == 1) {
            // Decline all requests
            habbo.getMessenger().handleAllRequests();
        }
        // else: invalid mode - do nothing
    }
}
