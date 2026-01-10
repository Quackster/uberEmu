package com.uber.server.game.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.Item;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.MarketplaceRepository;
import com.uber.server.game.users.inventory.UserItem;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Manages the marketplace system.
 */
public class Marketplace {
    private static final Logger logger = LoggerFactory.getLogger(Marketplace.class);
    
    private final MarketplaceRepository repository;
    private final Game game;
    private final Catalog catalog;
    
    public Marketplace(MarketplaceRepository repository, Game game, Catalog catalog) {
        this.repository = repository;
        this.game = game;
        this.catalog = catalog;
    }
    
    /**
     * Checks if an item can be sold on the marketplace.
     * @param item UserItem to check
     * @return True if item can be sold
     */
    public boolean canSellItem(UserItem item) {
        if (item == null || item.getBaseItem() == null) {
            return false;
        }
        
        return item.getBaseItem().allowTrade() && item.getBaseItem().allowMarketplaceSell();
    }
    
    /**
     * Calculates commission price for a selling price.
     * @param sellingPrice Selling price
     * @return Commission amount
     */
    public int calculateCommissionPrice(int sellingPrice) {
        return (int) Math.ceil(sellingPrice / 100.0);
    }
    
    /**
     * Gets the minimum timestamp for valid offers (current time - 2 days).
     * @return Minimum timestamp
     */
    public long formatTimestamp() {
        return TimeUtil.getUnixTimestamp() - 172800; // 2 days in seconds
    }
    
    /**
     * Sells an item on the marketplace.
     * @param client GameClient selling the item
     * @param itemId Item ID
     * @param sellingPrice Selling price
     */
    public void sellItem(GameClient client, long itemId, int sellingPrice) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getInventoryComponent() == null) {
            return;
        }
        
        UserItem item = habbo.getInventoryComponent().getItem(itemId);
        
        if (item == null || sellingPrice > 10000 || !canSellItem(item)) {
            ServerMessage response = new ServerMessage(610);
            response.appendBoolean(false);
            client.sendMessage(response);
            return;
        }
        
        int commission = calculateCommissionPrice(sellingPrice);
        int totalPrice = sellingPrice + commission;
        int itemType = item.getBaseItem().isWallItem() ? 2 : 1;
        long timestamp = TimeUtil.getUnixTimestamp();
        
        // Create offer in database
        if (repository.createOffer(item.getBaseItemId(), habbo.getId(), sellingPrice, totalPrice,
                                  item.getBaseItem().getPublicName(), item.getBaseItem().getSpriteId(),
                                  itemType, timestamp, item.getExtraData())) {
            // Remove item from inventory
            habbo.getInventoryComponent().removeItem(itemId);
            
            ServerMessage response = new ServerMessage(610);
            response.appendBoolean(true);
            client.sendMessage(response);
        } else {
            ServerMessage response = new ServerMessage(610);
            response.appendBoolean(false);
            client.sendMessage(response);
        }
    }
    
    /**
     * Serializes marketplace offers with filters.
     * @param minCost Minimum cost filter (-1 to ignore)
     * @param maxCost Maximum cost filter (-1 to ignore)
     * @param searchQuery Search query (empty to ignore)
     * @param filterMode Filter mode (1 = price DESC, 2 = price ASC)
     * @return ServerMessage with offers (ID 615)
     */
    public ServerMessage serializeOffers(int minCost, int maxCost, String searchQuery, int filterMode) {
        boolean orderByPriceDesc = (filterMode == 1);
        long minTimestamp = formatTimestamp();
        
        List<Map<String, Object>> offers = repository.getOffers(minCost, maxCost, searchQuery, 
                                                               orderByPriceDesc, minTimestamp);
        
        ServerMessage message = new ServerMessage(615);
        message.appendInt32(offers.size());
        
        for (Map<String, Object> offer : offers) {
            message.appendUInt(((Number) offer.get("offer_id")).longValue());
            message.appendInt32(2); // Unknown
            message.appendInt32(((Number) offer.get("item_type")).intValue());
            message.appendInt32(((Number) offer.get("sprite_id")).intValue());
            message.appendInt32(((Number) offer.get("total_price")).intValue());
            message.appendInt32(((Number) offer.get("sprite_id")).intValue()); // Duplicate?
            message.appendInt32(((Number) offer.get("total_price")).intValue()); // Average?
            message.appendInt32(1); // Number of offers
        }
        
        return message;
    }
    
    /**
     * Serializes user's own marketplace offers.
     * @param habboId User ID
     * @return ServerMessage with user's offers (ID 616)
     */
    public ServerMessage serializeOwnOffers(long habboId) {
        List<Map<String, Object>> offers = repository.getUserOffers(habboId);
        int profits = repository.getUserProfits(habboId);
        
        ServerMessage message = new ServerMessage(616);
        message.appendInt32(profits);
        message.appendInt32(offers.size());
        
        long currentTime = TimeUtil.getUnixTimestamp();
        
        for (Map<String, Object> offer : offers) {
            long timestamp = ((Number) offer.get("timestamp")).longValue();
            int minutesLeft = (int) Math.floor(((timestamp + 172800) - currentTime) / 60.0);
            int state = ((Number) offer.get("state")).intValue();
            
            // Check if expired
            if (minutesLeft <= 0) {
                state = 3; // Expired
                minutesLeft = 0;
            }
            
            message.appendUInt(((Number) offer.get("offer_id")).longValue());
            message.appendInt32(state); // 1 = active, 2 = sold, 3 = expired
            message.appendInt32(((Number) offer.get("item_type")).intValue());
            message.appendInt32(((Number) offer.get("sprite_id")).intValue());
            message.appendInt32(((Number) offer.get("total_price")).intValue());
            message.appendInt32(minutesLeft);
            message.appendInt32(((Number) offer.get("sprite_id")).intValue());
        }
        
        return message;
    }
}
