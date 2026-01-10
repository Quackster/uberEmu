package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for sending user message (message ID 462).
 */
public class ModSendUserMessageHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModSendUserMessageHandler.class);
    private final Game game;
    
    public ModSendUserMessageHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        String userMessage = message.popFixedString();
        
        com.uber.server.event.packet.help.ModSendUserMessageEvent event = new com.uber.server.event.packet.help.ModSendUserMessageEvent(
            client, message, userId, userMessage);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        userId = event.getUserId();
        userMessage = event.getUserMessage();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_alert")) {
            return;
        }
        
        game.getModerationTool().alertUser(client, userId, userMessage, false); // false = message, not caution
    }
}
