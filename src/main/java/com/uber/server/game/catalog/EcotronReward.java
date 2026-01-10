package com.uber.server.game.catalog;

import com.uber.server.game.items.Item;
import com.uber.server.game.items.ItemManager;

/**
 * Represents an ecotron reward.
 */
public class EcotronReward {
    private final long id;
    private final long displayId;
    private final long baseId;
    private final long rewardLevel;
    
    public EcotronReward(long id, long displayId, long baseId, long rewardLevel) {
        this.id = id;
        this.displayId = displayId;
        this.baseId = baseId;
        this.rewardLevel = rewardLevel;
    }
    
    public Item getBaseItem(ItemManager itemManager) {
        return itemManager.getItem(baseId);
    }
    
    // Getters
    public long getId() { return id; }
    public long getDisplayId() { return displayId; }
    public long getBaseId() { return baseId; }
    public long getRewardLevel() { return rewardLevel; }
}
