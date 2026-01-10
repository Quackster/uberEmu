package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting room chat log (message ID 456).
 */
public class ModGetRoomChatlogHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModGetRoomChatlogHandler.class);
    private final Game game;
    
    public ModGetRoomChatlogHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: Handler reads junk + roomId, but PacketEventFactory shows just roomId (int)
        // Adjusting to match event structure - reading roomId as int per PacketEventFactory
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.help.ModGetRoomChatlogEvent event = new com.uber.server.event.packet.help.ModGetRoomChatlogEvent(
            client, message, roomId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        roomId = event.getRoomId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_chatlogs")) {
            return;
        }
        
        // Check if room exists
        long roomIdLong = roomId;
        if (game.getRoomManager() != null && game.getRoomManager().getRoom(roomIdLong) != null) {
            try {
                client.sendMessage(game.getModerationTool().serializeRoomChatlog(roomIdLong));
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to serialize room chat log for room {}: {}", roomId, e.getMessage());
            }
        }
    }
}
