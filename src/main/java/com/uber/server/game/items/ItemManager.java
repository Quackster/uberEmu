package com.uber.server.game.items;

import com.uber.server.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages furniture item definitions.
 */
public class ItemManager {
    private static final Logger logger = LoggerFactory.getLogger(ItemManager.class);
    
    private final ConcurrentHashMap<Long, Item> items;
    private final ItemRepository itemRepository;
    
    public ItemManager(ItemRepository itemRepository) {
        this.items = new ConcurrentHashMap<>();
        this.itemRepository = itemRepository;
    }
    
    /**
     * Loads all items from the database.
     */
    public void loadItems() {
        items.clear();
        
        List<Map<String, Object>> itemData = itemRepository.loadAllItems();
        
        int loaded = 0;
        int failed = 0;
        
        for (Map<String, Object> row : itemData) {
            try {
                long id = ((Number) row.get("id")).longValue();
                int spriteId = ((Number) row.get("sprite_id")).intValue();
                String publicName = (String) row.get("public_name");
                String itemName = (String) row.get("item_name");
                String type = (String) row.get("type");
                int width = ((Number) row.get("width")).intValue();
                int length = ((Number) row.get("length")).intValue();
                double stackHeight = ((Number) row.get("stack_height")).doubleValue();
                
                // Parse boolean fields (stored as strings "1"/"0" or "true"/"false")
                boolean canStack = parseBoolean(row.get("can_stack"));
                boolean isWalkable = parseBoolean(row.get("is_walkable"));
                boolean canSit = parseBoolean(row.get("can_sit"));
                boolean allowRecycle = parseBoolean(row.get("allow_recycle"));
                boolean allowTrade = parseBoolean(row.get("allow_trade"));
                boolean allowMarketplaceSell = parseBoolean(row.get("allow_marketplace_sell"));
                boolean allowGift = parseBoolean(row.get("allow_gift"));
                boolean allowInventoryStack = parseBoolean(row.get("allow_inventory_stack"));
                
                String interactionType = (String) row.get("interaction_type");
                int interactionModesCount = ((Number) row.get("interaction_modes_count")).intValue();
                String vendingIds = (String) row.get("vending_ids");
                
                Item item = new Item(id, spriteId, publicName, itemName, type, width, length,
                        stackHeight, canStack, isWalkable, canSit, allowRecycle, allowTrade,
                        allowMarketplaceSell, allowGift, allowInventoryStack, interactionType,
                        interactionModesCount, vendingIds);
                
                items.put(id, item);
                loaded++;
            } catch (Exception e) {
                logger.error("Could not load item #{}: {}", row.get("id"), e.getMessage(), e);
                failed++;
            }
        }
        
        logger.info("Loaded {} item definition(s).", loaded);
        if (failed > 0) {
            logger.warn("{} item definition(s) could not be loaded.", failed);
        }
    }
    
    /**
     * Parses a boolean value from database (handles "1"/"0", "true"/"false", or actual boolean).
     */
    private boolean parseBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        String str = value.toString().trim();
        return "1".equals(str) || "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str);
    }
    
    /**
     * Checks if an item exists.
     * @param id Item ID
     * @return True if item exists
     */
    public boolean containsItem(long id) {
        return items.containsKey(id);
    }
    
    /**
     * Gets an item by ID.
     * @param id Item ID
     * @return Item, or null if not found
     */
    public Item getItem(long id) {
        return items.get(id);
    }
    
    /**
     * Gets the number of loaded items.
     * @return Number of items
     */
    public int getItemCount() {
        return items.size();
    }
}
