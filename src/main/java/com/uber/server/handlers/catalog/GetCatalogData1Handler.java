package com.uber.server.handlers.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting catalog data 1 (message ID 3011).
 * 
 * Note: This appears to be a hardcoded response
 */
public class GetCatalogData1Handler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetCatalogData1Handler.class);
    private final Game game;
    
    public GetCatalogData1Handler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.catalog.GetCatalogData1Event event = new com.uber.server.event.packet.catalog.GetCatalogData1Event(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        ServerMessage response = new ServerMessage(612);
        // Hardcoded values
        response.appendInt32(1);
        response.appendInt32(1);
        response.appendInt32(1);
        response.appendInt32(5);
        response.appendInt32(1);
        response.appendInt32(10000);
        response.appendInt32(48);
        response.appendInt32(7);
        client.sendMessage(response);
    }
}
