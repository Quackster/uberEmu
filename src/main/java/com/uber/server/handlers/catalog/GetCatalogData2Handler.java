package com.uber.server.handlers.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting catalog data 2 (message ID 473).
 * 
 * Note: This appears to be a hardcoded response
 */
public class GetCatalogData2Handler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetCatalogData2Handler.class);
    private final Game game;
    
    public GetCatalogData2Handler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.catalog.GetCatalogData2Event event = new com.uber.server.event.packet.catalog.GetCatalogData2Event(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        ServerMessage response = new ServerMessage(620);
        // Hardcoded values
        response.appendInt32(1);
        response.appendInt32(1);
        response.appendInt32(10);
        response.appendInt32(3064);
        response.appendInt32(3065);
        response.appendInt32(3066);
        response.appendInt32(3067);
        response.appendInt32(3068);
        response.appendInt32(3069);
        response.appendInt32(3070);
        response.appendInt32(3071);
        response.appendInt32(3072);
        response.appendInt32(3073);
        response.appendInt32(7);
        response.appendInt32(0);
        response.appendInt32(1);
        response.appendInt32(2);
        response.appendInt32(3);
        response.appendInt32(4);
        response.appendInt32(5);
        response.appendInt32(6);
        response.appendInt32(11);
        response.appendInt32(0);
        response.appendInt32(1);
        response.appendInt32(2);
        response.appendInt32(3);
        response.appendInt32(4);
        response.appendInt32(5);
        response.appendInt32(6);
        response.appendInt32(7);
        response.appendInt32(8);
        response.appendInt32(9);
        response.appendInt32(10);
        response.appendInt32(1);
        client.sendMessage(response);
    }
}
