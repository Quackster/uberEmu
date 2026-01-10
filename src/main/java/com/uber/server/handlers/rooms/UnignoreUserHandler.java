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
 * Handler for unignoring a user (message ID 322).
 * Ported from Messages/Requests/Rooms.cs UnignoreUser()
 * 
 * Note: The original C# implementation is commented out - placeholder for future implementation
 */
public class UnignoreUserHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(UnignoreUserHandler.class);
    private final Game game;
    
    public UnignoreUserHandler(Game game) {
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
        // if (habbo.getMutedUsers().contains(userId)) {
        //     habbo.getMutedUsers().remove(userId);
        //     ServerMessage response = new ServerMessage(419);
        //     response.appendInt32(3);
        //     client.sendMessage(response);
        // }
        
        logger.debug("UnignoreUser called by user {}", habbo.getId());
    }
}
