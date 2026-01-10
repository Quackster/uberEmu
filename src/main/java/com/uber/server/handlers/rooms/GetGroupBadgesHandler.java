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
 * Handler for getting group badges (message ID 230).
 * 
 * Note: Groups not yet implemented - returns hardcoded value
 */
public class GetGroupBadgesHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetGroupBadgesHandler.class);
    private final Game game;
    
    public GetGroupBadgesHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.room.GetGroupBadgesEvent event = new com.uber.server.event.packet.room.GetGroupBadgesEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Hardcoded group badges
        ServerMessage response = new ServerMessage(309);
        response.appendStringWithBreak("IcIrDs43103s19014d5a1dc291574a508bc80a64663e61a00");
        client.sendMessage(response);
    }
}
