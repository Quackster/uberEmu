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
 * Handler for purchasing from marketplace (message ID 3014).
 */
public class MarketplacePurchaseHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MarketplacePurchaseHandler.class);
    private final Game game;
    
    public MarketplacePurchaseHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int offerId = message.popWiredInt32();
        
        com.uber.server.event.packet.catalog.MarketplacePurchaseEvent event = new com.uber.server.event.packet.catalog.MarketplacePurchaseEvent(client, message, offerId);
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
            client.sendNotif("Sorry, this offer has expired.");
            return;
        }
        
        // Check state and timestamp
        int state = ((Number) offer.get("state")).intValue();
        long timestamp = ((Number) offer.get("timestamp")).longValue();
        long minTimestamp = game.getCatalog().getMarketplace().formatTimestamp();
        
        if (state != 1 || timestamp <= minTimestamp) {
            client.sendNotif("Sorry, this offer has expired.");
            return;
        }
        
        // Get item
        long itemId = ((Number) offer.get("item_id")).longValue();
        Item item = game.getItemManager().getItem(itemId);
        if (item == null) {
            return;
        }
        
        // Deduct credits
        int totalPrice = ((Number) offer.get("total_price")).intValue();
        if (totalPrice >= 1) {
            if (habbo.getCredits() < totalPrice) {
                return; // Not enough credits
            }
            habbo.setCredits(habbo.getCredits() - totalPrice);
            habbo.updateCreditsBalance(game.getUserRepository(), true);
        }
        
        // Deliver item
        String extraData = (String) offer.get("extra_data");
        game.getCatalog().deliverItems(client, item, 1, extraData != null ? extraData : "");
        
        // Update offer state to sold (2)
        repository.updateOfferState(offerIdLong, 2);
        
        // Send purchase confirmation
        ServerMessage response = new ServerMessage(67);
        response.appendUInt(item.getId());
        response.appendStringWithBreak(item.getItemName());
        response.appendInt32(0);
        response.appendInt32(0);
        response.appendInt32(1);
        response.appendStringWithBreak(item.getType().toLowerCase());
        response.appendInt32(item.getSpriteId());
        response.appendStringWithBreak("");
        response.appendInt32(1);
        response.appendInt32(-1);
        response.appendStringWithBreak("");
        client.sendMessage(response);
        
        // Refresh marketplace offers
        client.sendMessage(game.getCatalog().getMarketplace().serializeOffers(-1, -1, "", 1));
    }
}
