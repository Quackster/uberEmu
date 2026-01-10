package com.uber.server.messages.incoming.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetCatalogIndexComposer (ID 101).
 * Processes catalog index requests from the client.
 */
public class GetCatalogIndexComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetCatalogIndexComposerHandler.class);
    private final Game game;
    
    public GetCatalogIndexComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.catalog.GetCatalogIndexEvent event = new com.uber.server.event.packet.catalog.GetCatalogIndexEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        var catalog = game.getCatalog();
        if (catalog == null) {
            logger.warn("Catalog is not initialized");
            return;
        }
        
        // Use the catalog's serializeIndex method which returns message ID 126
        // TODO: Replace with CatalogIndexEventComposer
        var response = catalog.serializeIndex(client);
        client.sendMessage(response);
    }
}
