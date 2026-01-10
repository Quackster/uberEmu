package com.uber.server.handlers.rooms;

import com.uber.server.game.catalog.Catalog;
import com.uber.server.game.catalog.EcotronReward;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.Item;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.users.inventory.UserItem;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Handler for recycling items (message ID 414).
 */
public class RecycleItemsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RecycleItemsHandler.class);
    private final Game game;
    
    public RecycleItemsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        java.util.List<Long> itemIds = new java.util.ArrayList<>();
        int recycleCount = message.popWiredInt32();
        for (int i = 0; i < recycleCount; i++) {
            itemIds.add(message.popWiredUInt());
        }
        
        com.uber.server.event.packet.room.RecycleItemsEvent event = new com.uber.server.event.packet.room.RecycleItemsEvent(client, message, itemIds);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        itemIds = event.getItemIds();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        int itemCount = itemIds.size();
        
        // Must recycle exactly 5 items
        if (itemCount != 5) {
            return;
        }
        
        // Collect items to recycle
        for (long itemId : itemIds) {
            UserItem item = habbo.getInventoryComponent().getItem(itemId);
            
            if (item == null || item.getBaseItem() == null || !item.getBaseItem().allowRecycle()) {
                return; // Invalid item or not recyclable
            }
            
            // Remove item from inventory
            habbo.getInventoryComponent().removeItem(itemId);
        }
        
        // Generate new present item
        Catalog catalog = game.getCatalog();
        if (catalog == null) {
            return;
        }
        
        long newItemId = game.getCatalogRepository().generateItemId();
        EcotronReward reward = catalog.getRandomEcotronReward();
        
        if (reward == null) {
            return;
        }
        
        // Create present item
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        game.getInventoryRepository().createUserItem(newItemId, habbo.getId(), 1478, dateStr);
        
        // Create present data
        Item baseItem = game.getItemManager().getItem(reward.getBaseId());
        if (baseItem != null) {
            game.getInventoryRepository().createUserPresent(newItemId, reward.getBaseId(), 1, "");
        }
        
        // Update inventory
        habbo.getInventoryComponent().updateItems(true);
        
        // Send response
        ServerMessage response = new ServerMessage(508);
        response.appendBoolean(true);
        response.appendUInt(newItemId);
        client.sendMessage(response);
    }
}
