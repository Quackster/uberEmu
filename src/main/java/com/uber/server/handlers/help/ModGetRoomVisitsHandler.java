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
        message.resetPointer();
        
        // Note: PacketEventFactory shows roomId as int, but handler reads userId as long
        // Checking PacketEventFactory shows it reads roomId, but handler seems to read userId
        // For now matching handler logic (userId as long)
        long userId = message.popWiredUInt();
        
        // Note: ModGetRoomVisitsEvent expects int roomId per PacketEventFactory, but handler reads userId
        // Using GenericPacketEvent due to mismatch
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 458);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Re-read userId after event (GenericPacketEvent doesn't store fields)
        message.resetPointer();
        userId = message.popWiredUInt();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_mod")) {
            return;
        }
        
        com.uber.server.messages.ServerMessage visitsMessage = game.getModerationTool().serializeRoomVisits(userId);
        if (visitsMessage != null) {
            client.sendMessage(visitsMessage);
        }
    }
}
