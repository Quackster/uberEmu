package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting room visits for a user (message ID 458).
 */
public class ModGetRoomVisitsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModGetRoomVisitsHandler.class);
    private final Game game;
    
    public ModGetRoomVisitsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_mod")) {
            return;
        }
        
        long userId = message.popWiredUInt();
        
        com.uber.server.messages.ServerMessage visitsMessage = game.getModerationTool().serializeRoomVisits(userId);
        if (visitsMessage != null) {
            client.sendMessage(visitsMessage);
        }
    }
}
