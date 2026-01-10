package com.uber.server.messages.incoming.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetCreditsInfoComposer (ID 8).
 * Processes credit balance requests from the client.
 */
public class GetCreditsInfoComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetCreditsInfoComposerHandler.class);
    private final Game game;
    
    public GetCreditsInfoComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.user.GetCreditsInfoEvent event = new com.uber.server.event.packet.user.GetCreditsInfoEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
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
