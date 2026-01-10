package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for sending user caution (message ID 461).
 */
public class ModSendUserCautionHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModSendUserCautionHandler.class);
    private final Game game;
    
    public ModSendUserCautionHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        String cautionMessage = message.popFixedString();
        
        com.uber.server.event.packet.help.ModSendUserCautionEvent event = new com.uber.server.event.packet.help.ModSendUserCautionEvent(
            client, message, userId, cautionMessage);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        userId = event.getUserId();
        cautionMessage = event.getCautionMessage();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_alert")) {
            return;
        }
        
        game.getModerationTool().alertUser(client, userId, cautionMessage, true); // true = caution
    }
}
