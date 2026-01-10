package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for kicking a user (message ID 463).
 */
public class ModKickUserHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModKickUserHandler.class);
    private final Game game;
    
    public ModKickUserHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        String kickMessage = message.popFixedString();
        
        com.uber.server.event.packet.help.ModKickUserEvent event = new com.uber.server.event.packet.help.ModKickUserEvent(
            client, message, userId, kickMessage);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        userId = event.getUserId();
        kickMessage = event.getKickMessage();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_kick")) {
            return;
        }
        
        game.getModerationTool().kickUser(client, userId, kickMessage, false); // false = hard kick
    }
}
