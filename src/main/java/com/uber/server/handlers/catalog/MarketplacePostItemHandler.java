package com.uber.server.handlers.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for posting an item to marketplace (message ID 3010).
 */
public class MarketplacePostItemHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MarketplacePostItemHandler.class);
    private final Game game;
    
    public MarketplacePostItemHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long itemId = message.popWiredUInt();
        int price = message.popWiredInt32();
        
        com.uber.server.event.packet.catalog.MarketplacePostItemEvent event = new com.uber.server.event.packet.catalog.MarketplacePostItemEvent(client, message, itemId, price);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        itemId = event.getItemId();
        price = event.getPrice();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getInventoryComponent() == null) {
            return;
        }
        
        int sellingPrice = price;
        
        com.uber.server.game.users.inventory.UserItem item = habbo.getInventoryComponent().getItem(itemId);
        if (item == null || item.getBaseItem() == null || !item.getBaseItem().allowTrade()) {
            return;
        }
        
        if (game.getCatalog() != null && game.getCatalog().getMarketplace() != null) {
            game.getCatalog().getMarketplace().sellItem(client, itemId, sellingPrice);
        }
    }
}
