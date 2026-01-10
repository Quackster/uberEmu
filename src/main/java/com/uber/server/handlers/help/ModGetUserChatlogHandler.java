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
 * Ported from Messages/Requests/Help.cs ModGetUserChatlog()
 */
public class ModGetUserChatlogHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModGetUserChatlogHandler.class);
    private final Game game;
    
    public ModGetUserChatlogHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_chatlogs")) {
            return;
        }
        
        long userId = message.popWiredUInt();
        
        com.uber.server.messages.ServerMessage chatlogMessage = game.getModerationTool().serializeUserChatlog(userId);
        if (chatlogMessage != null) {
            client.sendMessage(chatlogMessage);
        }
    }
}
