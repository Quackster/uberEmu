package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for performing room action (message ID 460).
 */
public class ModPerformRoomActionHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModPerformRoomActionHandler.class);
    private final Game game;
    
    public ModPerformRoomActionHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: Handler reads roomId + 3 booleans, but ModPerformRoomActionEvent expects roomId + action (int)
        // Handler logic combines booleans into actions, so using GenericPacketEvent due to structure mismatch
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 460);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_mod")) {
            return;
        }
        
        // Re-read after event (GenericPacketEvent doesn't store fields)
        message.resetPointer();
        long roomId = message.popWiredUInt();
        boolean actOne = message.popWiredBoolean(); // Set room lock to doorbell
        boolean actTwo = message.popWiredBoolean(); // Set room to inappropriate
        boolean actThree = message.popWiredBoolean(); // Kick all users
        
        game.getModerationTool().performRoomAction(client, roomId, actThree, actOne, actTwo);
    }
}
