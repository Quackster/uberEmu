package com.uber.server.handlers.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.repository.MarketplaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Handler for claiming marketplace credits (message ID 3016).
 */
public class MarketplaceClaimCreditsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MarketplaceClaimCreditsHandler.class);
    private final Game game;
    
    public MarketplaceClaimCreditsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.catalog.MarketplaceClaimCreditsEvent event = new com.uber.server.event.packet.catalog.MarketplaceClaimCreditsEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        MarketplaceRepository repository = game.getMarketplaceRepository();
        int profits = repository.getUserProfits(habbo.getId());
        
        if (profits >= 1) {
            habbo.setCredits(habbo.getCredits() + profits);
            habbo.updateCreditsBalance(game.getUserRepository());
            
            // Delete sold offers (state = 2)
            repository.deleteSoldOffers(habbo.getId());
        }
    }
}
