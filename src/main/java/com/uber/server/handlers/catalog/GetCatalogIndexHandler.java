package com.uber.server.handlers.catalog;

import com.uber.server.game.catalog.Catalog;
import com.uber.server.game.catalog.CatalogPage;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Handler for getting catalog index (message ID 101).
 */
public class GetCatalogIndexHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetCatalogIndexHandler.class);
    private final Game game;
    
    public GetCatalogIndexHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.catalog.GetCatalogIndexEvent event = new com.uber.server.event.packet.catalog.GetCatalogIndexEvent(
            client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        Catalog catalog = game.getCatalog();
        if (catalog == null) {
            logger.warn("Catalog is not initialized");
            return;
        }
        
        // Use the catalog's serializeIndex method which returns message ID 126
        ServerMessage response = catalog.serializeIndex(client);
        client.sendMessage(response);
    }
}
