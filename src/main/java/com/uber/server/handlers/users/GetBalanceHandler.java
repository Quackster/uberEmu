package com.uber.server.handlers.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting user balance (message ID 8).
 * Ported from Messages/Requests/Users.cs GetBalance()
 */
public class GetBalanceHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetBalanceHandler.class);
    private final Game game;
    
    public GetBalanceHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        // Update credits balance
        habbo.updateCreditsBalance(game.getUserRepository(), false);
        
        // Update activity points balance
        habbo.updateActivityPointsBalance(false);
    }
}
