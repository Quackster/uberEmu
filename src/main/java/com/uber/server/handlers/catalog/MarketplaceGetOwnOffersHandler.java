package com.uber.server.handlers.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting own marketplace offers (message ID 3019).
 */
public class MarketplaceGetOwnOffersHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MarketplaceGetOwnOffersHandler.class);
    private final Game game;
    
    public MarketplaceGetOwnOffersHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.catalog.MarketplaceGetOwnOffersEvent event = new com.uber.server.event.packet.catalog.MarketplaceGetOwnOffersEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        if (game.getCatalog() != null && game.getCatalog().getMarketplace() != null) {
            client.sendMessage(game.getCatalog().getMarketplace().serializeOwnOffers(habbo.getId()));
        }
    }
}
