package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting user chat log (message ID 455).
 */
public class ModGetUserChatlogHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModGetUserChatlogHandler.class);
    private final Game game;
    
    public ModGetUserChatlogHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        
        com.uber.server.event.packet.help.ModGetUserChatlogEvent event = new com.uber.server.event.packet.help.ModGetUserChatlogEvent(
            client, message, userId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        userId = event.getUserId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_chatlogs")) {
            return;
        }
        
        com.uber.server.messages.ServerMessage chatlogMessage = game.getModerationTool().serializeUserChatlog(userId);
        if (chatlogMessage != null) {
            client.sendMessage(chatlogMessage);
        }
    }
}
