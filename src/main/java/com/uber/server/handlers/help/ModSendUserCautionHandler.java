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
 * Ported from Messages/Requests/Help.cs ModSendUserCaution()
 */
public class ModSendUserCautionHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModSendUserCautionHandler.class);
    private final Game game;
    
    public ModSendUserCautionHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_alert")) {
            return;
        }
        
        long userId = message.popWiredUInt();
        String cautionMessage = message.popFixedString();
        
        game.getModerationTool().alertUser(client, userId, cautionMessage, true); // true = caution
    }
}
