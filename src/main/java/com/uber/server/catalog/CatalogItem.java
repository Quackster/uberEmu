package com.uber.server.catalog;

import com.uber.server.items.Item;
import com.uber.server.items.ItemManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a catalog item.
 * Ported from HabboHotel/Catalogs/CatalogItem.cs
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
}
