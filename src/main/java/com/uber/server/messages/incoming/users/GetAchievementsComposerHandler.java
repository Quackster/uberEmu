package com.uber.server.messages.incoming.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetAchievementsComposer (ID 370).
 * Processes achievement requests from the client.
 */
public class GetAchievementsComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetAchievementsComposerHandler.class);
    private final Game game;
    
    public GetAchievementsComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.user.GetAchievementsEvent event = new com.uber.server.event.packet.user.GetAchievementsEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        if (game.getAchievementManager() == null) {
            return;
        }
        
        // serializeAchievementList already wraps in AchievementsListMessageEventComposer
        client.sendMessage(game.getAchievementManager().serializeAchievementList(client));
    }
}
