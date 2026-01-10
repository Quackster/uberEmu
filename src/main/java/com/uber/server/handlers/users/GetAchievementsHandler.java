package com.uber.server.handlers.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting user achievements (message ID 370).
 */
public class GetAchievementsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetAchievementsHandler.class);
    private final Game game;
    
    public GetAchievementsHandler(Game game) {
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
        
        client.sendMessage(game.getAchievementManager().serializeAchievementList(client));
    }
}
