package com.uber.server.game.catalog;

import com.uber.server.game.items.Item;
import com.uber.server.game.items.ItemManager;
import com.uber.server.messages.ServerMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a catalog item.
 */
public class CatalogItem {
    private final long id;
    private final List<Long> itemIds;
    private final String name;
    private final int creditsCost;
    private final int pixelsCost;
    private final int amount;
    
    public CatalogItem(long id, String name, String itemIdsStr, int creditsCost, int pixelsCost, int amount) {
        this.id = id;
        this.name = name;
        this.itemIds = new ArrayList<>();
        
        // Parse comma-separated item IDs
        if (itemIdsStr != null && !itemIdsStr.isEmpty()) {
            String[] parts = itemIdsStr.split(",");
            for (String part : parts) {
                try {
                    this.itemIds.add(Long.parseLong(part.trim()));
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }
        
        this.creditsCost = creditsCost;
        this.pixelsCost = pixelsCost;
        this.amount = amount;
    }
    
    public boolean isDeal() {
        return itemIds.size() > 1;
    }
    
    public Item getBaseItem(ItemManager itemManager) {
        if (isDeal() || itemIds.isEmpty()) {
            return null;
        }
        return itemManager.getItem(itemIds.get(0));
    }
    
    // Getters
    public long getId() { return id; }
    public List<Long> getItemIds() { return itemIds; }
    public String getName() { return name; }
    public int getCreditsCost() { return creditsCost; }
    public int getPixelsCost() { return pixelsCost; }
    public int getAmount() { return amount; }
    
    /**
     * Serializes this catalog item.
     * @param message ServerMessage to append to
     * @param itemManager ItemManager to get base item info
     */
    public void serialize(ServerMessage message, ItemManager itemManager) {
        if (isDeal()) {
            // Deals with multiple items are not fully supported yet
            // Multi-item deals are not yet fully supported
            // For now, we'll serialize as a regular item using the first item ID
            Item baseItem = itemManager.getItem(itemIds.get(0));
            if (baseItem == null) {
                return;
            }
            
            message.appendUInt(id);
            message.appendStringWithBreak(name);
            message.appendInt32(creditsCost);
            message.appendInt32(pixelsCost);
            message.appendInt32(1);
            message.appendStringWithBreak(baseItem.getType());
            message.appendInt32(baseItem.getSpriteId());
            message.appendStringWithBreak("");
            message.appendInt32(amount);
            message.appendInt32(-1);
        } else {
            Item baseItem = getBaseItem(itemManager);
            if (baseItem == null) {
                return;
            }
            
            message.appendUInt(id);
            message.appendStringWithBreak(name);
            message.appendInt32(creditsCost);
            message.appendInt32(pixelsCost);
            message.appendInt32(1);
            message.appendStringWithBreak(baseItem.getType());
            message.appendInt32(baseItem.getSpriteId());
            message.appendStringWithBreak("");
            message.appendInt32(amount);
            message.appendInt32(-1);
        }
    }
}
