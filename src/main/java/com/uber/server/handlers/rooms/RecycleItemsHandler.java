package com.uber.server.handlers.rooms;

import com.uber.server.catalog.Catalog;
import com.uber.server.catalog.EcotronReward;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.items.Item;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.users.inventory.UserItem;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Handler for recycling items (message ID 414).
 * Ported from Messages/Requests/Rooms.cs RecycleItems()
 */
public class RecycleItemsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RecycleItemsHandler.class);
    private final Game game;
    
    public RecycleItemsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        int itemCount = message.popWiredInt32();
        
        // Must recycle exactly 5 items
        if (itemCount != 5) {
            return;
        }
        
        // Collect items to recycle
        for (int i = 0; i < itemCount; i++) {
            long itemId = message.popWiredUInt();
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
