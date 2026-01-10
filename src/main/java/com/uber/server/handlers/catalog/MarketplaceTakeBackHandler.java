package com.uber.server.handlers.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.items.Item;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.MarketplaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Handler for taking back a marketplace offer (message ID 3015).
 * Ported from Messages/Requests/Catalog.cs MarketplaceTakeBack()
 */
public class MarketplaceTakeBackHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MarketplaceTakeBackHandler.class);
    private final Game game;
    
    public MarketplaceTakeBackHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        long offerId = message.popWiredUInt();
        MarketplaceRepository repository = game.getMarketplaceRepository();
        
        Map<String, Object> offer = repository.getOffer(offerId);
        if (offer == null) {
            return;
        }
        
        // Verify ownership and state
        long userId = ((Number) offer.get("user_id")).longValue();
        int state = ((Number) offer.get("state")).intValue();
        
        if (userId != habbo.getId() || state != 1) {
            return; // Not owner or not active
        }
        
        // Get item and deliver it
        long itemId = ((Number) offer.get("item_id")).longValue();
        Item item = game.getItemManager().getItem(itemId);
        if (item == null) {
            return;
        }
        
        String extraData = (String) offer.get("extra_data");
        if (game.getCatalog() != null) {
            game.getCatalog().deliverItems(client, item, 1, extraData != null ? extraData : "");
        }
        
        // Delete offer
        repository.deleteOffer(offerId);
        
        ServerMessage response = new ServerMessage(614);
        response.appendUInt(offerId);
        response.appendBoolean(true);
        client.sendMessage(response);
    }
}
