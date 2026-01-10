package com.uber.server.handlers.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting marketplace offers (message ID 3018).
 */
public class MarketplaceGetOffersHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MarketplaceGetOffersHandler.class);
    private final Game game;
    
    public MarketplaceGetOffersHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String searchQuery = message.popFixedString();
        int minPrice = message.popWiredInt32();
        int maxPrice = message.popWiredInt32();
        
        com.uber.server.event.packet.catalog.MarketplaceGetOffersEvent event = new com.uber.server.event.packet.catalog.MarketplaceGetOffersEvent(client, message, searchQuery, minPrice, maxPrice);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        searchQuery = event.getSearchQuery();
        minPrice = event.getMinPrice();
        maxPrice = event.getMaxPrice();
        
        int filterMode = message.popWiredInt32();
        
        if (game.getCatalog() != null && game.getCatalog().getMarketplace() != null) {
            client.sendMessage(game.getCatalog().getMarketplace().serializeOffers(minPrice, maxPrice, searchQuery, filterMode));
        }
    }
}
