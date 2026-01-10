package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for ignoring a user (message ID 319).
 * Ported from Messages/Requests/Rooms.cs IgnoreUser()
 * 
 * Note: The original C# implementation is commented out - placeholder for future implementation
 */
public class IgnoreUserHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(IgnoreUserHandler.class);
    private final Game game;
    
    public IgnoreUserHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        // Original C# implementation is commented out - placeholder for future implementation
        // long userId = message.popWiredUInt();
        // if (!habbo.getMutedUsers().contains(userId)) {
        //     habbo.getMutedUsers().add(userId);
        //     ServerMessage response = new ServerMessage(419);
        //     response.appendInt32(1);
        //     client.sendMessage(response);
        // }
        
        logger.debug("IgnoreUser called by user {}", habbo.getId());
    }
}
