package com.uber.server.messages.incoming.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetBadgesComposer (ID 157).
 * Processes badge requests from the client.
 */
public class GetBadgesComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetBadgesComposerHandler.class);
    private final Game game;
    
    public GetBadgesComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.user.GetBadgesEvent event = new com.uber.server.event.packet.user.GetBadgesEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        if (habbo.getBadgeComponent() == null) {
            return;
        }
        
        // TODO: Replace with BadgesEventComposer (ID 229)
        client.sendMessage(habbo.getBadgeComponent().serialize());
    }
}
