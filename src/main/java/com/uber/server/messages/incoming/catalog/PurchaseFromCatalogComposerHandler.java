package com.uber.server.messages.incoming.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for PurchaseFromCatalogComposer (ID 100).
 * Processes catalog purchase requests from the client.
 */
public class PurchaseFromCatalogComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseFromCatalogComposerHandler.class);
    private final Game game;
    
    public PurchaseFromCatalogComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        if (client.getHabbo() == null) {
            return;
        }
        
        int pageId = message.popWiredInt32();
        long itemId = message.popWiredUInt();
        String extraData = message.popFixedString();
        
        // Call catalog handlePurchase (not a gift purchase)
        game.getCatalog().handlePurchase(client, pageId, itemId, extraData, false, "", "");
    }
}
