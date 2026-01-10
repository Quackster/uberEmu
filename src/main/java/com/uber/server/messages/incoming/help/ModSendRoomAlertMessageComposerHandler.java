package com.uber.server.messages.incoming.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for ModSendRoomAlertMessageComposer (ID 200).
 * Processes moderation room alert requests from the client.
 */
public class ModSendRoomAlertMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModSendRoomAlertMessageComposerHandler.class);
    private final Game game;
    
    public ModSendRoomAlertMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        String alertMessage = message.popFixedString();
        
        com.uber.server.event.packet.help.ModSendRoomAlertEvent event = new com.uber.server.event.packet.help.ModSendRoomAlertEvent(client, message, roomId, alertMessage);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        roomId = event.getRoomId();
        alertMessage = event.getMessage();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_alert")) {
            return;
        }
        
        long roomIdLong = roomId;
        boolean caution = true; // Default to caution
        
        game.getModerationTool().roomAlert(roomIdLong, caution, alertMessage);
    }
}
