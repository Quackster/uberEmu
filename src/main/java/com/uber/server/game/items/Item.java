package com.uber.server.game.items;

/**
 * Represents a furniture item definition.
 */
public class Item {
    private final long id;
    private final int spriteId;
    private final String publicName;
    private final String itemName;
    private final String type;
    private final int width;
    private final int length;
    private final double stackHeight;
    private final boolean canStack;
    private final boolean isWalkable;
    private final boolean canSit;
    private final boolean allowRecycle;
    private final boolean allowTrade;
    private final boolean allowMarketplaceSell;
    private final boolean allowGift;
    private final boolean allowInventoryStack;
    private final String interactionType;
    private final int interactionModesCount;
    private final String vendingIds;
    
    public Item(long id, int spriteId, String publicName, String itemName, String type,
                int width, int length, double stackHeight, boolean canStack, boolean isWalkable,
                boolean canSit, boolean allowRecycle, boolean allowTrade, boolean allowMarketplaceSell,
                boolean allowGift, boolean allowInventoryStack, String interactionType,
                int interactionModesCount, String vendingIds) {
        this.id = id;
        this.spriteId = spriteId;
        this.publicName = publicName;
        this.itemName = itemName;
        this.type = type != null ? type.toLowerCase() : "";
        this.width = width;
        this.length = length;
        this.stackHeight = stackHeight;
        this.canStack = canStack;
        this.isWalkable = isWalkable;
        this.canSit = canSit;
        this.allowRecycle = allowRecycle;
        this.allowTrade = allowTrade;
        this.allowMarketplaceSell = allowMarketplaceSell;
        this.allowGift = allowGift;
        this.allowInventoryStack = allowInventoryStack;
        this.interactionType = interactionType != null ? interactionType.toLowerCase() : "";
        this.interactionModesCount = interactionModesCount;
        this.vendingIds = vendingIds;
    }
    
    // Getters
    public long getId() { return id; }
    public int getSpriteId() { return spriteId; }
    public String getPublicName() { return publicName; }
    public String getItemName() { return itemName; }
    public String getType() { return type; }
    public int getWidth() { return width; }
    public int getLength() { return length; }
    public double getStackHeight() { return stackHeight; }
    public double getHeight() { return stackHeight; }
    public boolean canStack() { return canStack; }
    public boolean isWalkable() { return isWalkable; }
    public boolean canSit() { return canSit; }
    public boolean allowRecycle() { return allowRecycle; }
    public boolean allowTrade() { return allowTrade; }
    public boolean allowMarketplaceSell() { return allowMarketplaceSell; }
    public boolean allowGift() { return allowGift; }
    public boolean allowInventoryStack() { return allowInventoryStack; }
    public String getInteractionType() { return interactionType; }
    public int getInteractionModesCount() { return interactionModesCount; }
    public String getVendingIds() { return vendingIds; }
    
    public boolean isFloorItem() {
        return "s".equals(type);
    }
    
    public boolean isWallItem() {
        return "i".equals(type);
    }
}
