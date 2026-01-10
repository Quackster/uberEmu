package com.uber.server.game.users.inventory;

import com.uber.server.game.items.Item;
import com.uber.server.game.items.ItemManager;
import com.uber.server.messages.ServerMessage;

/**
 * Represents a user's inventory item.
 */
public class UserItem {
    private final long id;
    private final long baseItem;
    private final String extraData;
    private final ItemManager itemManager;
    
    public UserItem(long id, long baseItem, String extraData, ItemManager itemManager) {
        this.id = id;
        this.baseItem = baseItem;
        this.extraData = extraData != null ? extraData : "";
        this.itemManager = itemManager;
    }
    
    public long getId() {
        return id;
    }
    
    public long getBaseItemId() {
        return baseItem;
    }
    
    public String getExtraData() {
        return extraData;
    }
    
    /**
     * Gets the base item definition.
     * @return Item object, or null if not found
     */
    public Item getBaseItem() {
        if (itemManager == null) {
            return null;
        }
        return itemManager.getItem(baseItem);
    }
    
    /**
     * Serializes the item to a ServerMessage.
     * @param message ServerMessage to append to
     * @param inventory True if serializing for inventory, false for room
     */
    public void serialize(ServerMessage message, boolean inventory) {
        Item base = getBaseItem();
        if (base == null) {
            return;
        }
        
        message.appendUInt(id);
        message.appendInt32(0);
        message.appendStringWithBreak(base.getType().toUpperCase());
        message.appendUInt(id);
        message.appendInt32(base.getSpriteId());
        
        // Determine item category
        String name = base.getItemName().toLowerCase();
        if (name.contains("a2")) {
            message.appendInt32(3);
        } else if (name.contains("wallpaper")) {
            message.appendInt32(2);
        } else if (name.contains("landscape")) {
            message.appendInt32(4);
        } else {
            message.appendInt32(0);
        }
        
        message.appendStringWithBreak(extraData != null ? extraData : "");
        message.appendBoolean(base.allowRecycle());
        message.appendBoolean(base.allowTrade());
        message.appendBoolean(base.allowInventoryStack());
        
        // Marketplace check
        message.appendBoolean(base.allowMarketplaceSell());
        
        message.appendInt32(-1);
        
        if ("s".equalsIgnoreCase(base.getType())) {
            message.appendStringWithBreak("");
            message.appendInt32(-1);
        }
    }
}
