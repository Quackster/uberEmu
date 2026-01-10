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
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_chatlogs")) {
            return;
        }
        
        int junk = message.popWiredInt32(); // Unused
        long roomId = message.popWiredUInt();
        
        // Check if room exists
        if (game.getRoomManager() != null && game.getRoomManager().getRoom(roomId) != null) {
            try {
                client.sendMessage(game.getModerationTool().serializeRoomChatlog(roomId));
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to serialize room chat log for room {}: {}", roomId, e.getMessage());
            }
        }
    }
}
