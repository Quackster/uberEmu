package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for sending room alert (message ID 200).
 */
public class ModSendRoomAlertHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModSendRoomAlertHandler.class);
    private final Game game;
    
    public ModSendRoomAlertHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: Handler reads one, two, alertMessage and gets roomId from current room
        // ModSendRoomAlertEvent expects roomId from message + alertMessage
        // Using GenericPacketEvent due to structure mismatch
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 200);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_alert")) {
            return;
        }
        
        // Re-read after event
        message.resetPointer();
        int one = message.popWiredInt32(); // Unused
        int two = message.popWiredInt32();
        String alertMessage = message.popFixedString();
        
        long roomId = habbo.getCurrentRoomId();
        boolean caution = (two != 3); // If two == 3, it's a message, not a caution
        
        game.getModerationTool().roomAlert(roomId, caution, alertMessage);
    }
}
