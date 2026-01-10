package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for banning a user (message ID 464).
 */
public class ModBanUserHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModBanUserHandler.class);
    private final Game game;
    
    public ModBanUserHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        String banMessage = message.popFixedString();
        int banHours = message.popWiredInt32();
        
        com.uber.server.event.packet.help.ModBanUserEvent event = new com.uber.server.event.packet.help.ModBanUserEvent(
            client, message, userId, banMessage, banHours);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        userId = event.getUserId();
        banMessage = event.getMessage();
        banHours = event.getBanHours();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_ban")) {
            return;
        }
        
        long lengthSeconds = banHours * 3600L; // Convert hours to seconds
        
        game.getModerationTool().banUser(client, userId, lengthSeconds, banMessage);
    }
}
