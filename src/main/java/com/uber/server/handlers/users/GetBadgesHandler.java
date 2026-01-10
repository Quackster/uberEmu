package com.uber.server.handlers.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting user badges (message ID 157).
 */
public class GetBadgesHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetBadgesHandler.class);
    private final Game game;
    
    public GetBadgesHandler(Game game) {
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
        
        client.sendMessage(habbo.getBadgeComponent().serialize());
    }
}
