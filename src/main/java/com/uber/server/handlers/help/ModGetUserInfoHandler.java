package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting user info (message ID 454).
 */
public class ModGetUserInfoHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ModGetUserInfoHandler.class);
    private final Game game;
    
    public ModGetUserInfoHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        
        com.uber.server.event.packet.help.ModGetUserInfoEvent event = new com.uber.server.event.packet.help.ModGetUserInfoEvent(
            client, message, userId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        userId = event.getUserId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.hasFuse("fuse_mod")) {
            return;
        }
        
        // Check if user exists (check if name is not "Unknown User")
        String userName = game.getClientManager() != null ? 
                         game.getClientManager().getNameById(userId) : "";
        
        if (userName != null && !userName.isEmpty() && !"Unknown User".equals(userName)) {
            try {
                client.sendMessage(game.getModerationTool().serializeUserInfo(userId));
            } catch (IllegalArgumentException e) {
                client.sendNotif("Could not load user info; invalid user.");
            }
        } else {
            client.sendNotif("Could not load user info; invalid user.");
        }
    }
}
