package com.uber.server.handlers.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.Item;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.MarketplaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Handler for taking back a marketplace offer (message ID 3015).
 */
public class MarketplaceTakeBackHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MarketplaceTakeBackHandler.class);
    private final Game game;
    
    public MarketplaceTakeBackHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int offerId = message.popWiredInt32();
        
        com.uber.server.event.packet.catalog.MarketplaceTakeBackEvent event = new com.uber.server.event.packet.catalog.MarketplaceTakeBackEvent(client, message, offerId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        offerId = event.getOfferId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        long offerIdLong = offerId;
        MarketplaceRepository repository = game.getMarketplaceRepository();
        
        Map<String, Object> offer = repository.getOffer(offerIdLong);
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
        repository.deleteOffer(offerIdLong);
        
        ServerMessage response = new ServerMessage(614);
        response.appendUInt(offerIdLong);
        response.appendBoolean(true);
        client.sendMessage(response);
    }
}
