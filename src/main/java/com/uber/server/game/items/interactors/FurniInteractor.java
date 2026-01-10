package com.uber.server.game.items.interactors;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;

/**
 * Abstract base class for furniture interactors.
 */
public abstract class FurniInteractor {
    /**
     * Called when an item is placed in a room.
     * @param session GameClient session that placed the item
     * @param item RoomItem that was placed
     */
    public abstract void onPlace(GameClient session, RoomItem item);
    
    /**
     * Called when an item is removed from a room.
     * @param session GameClient session that removed the item
     * @param item RoomItem that was removed
     */
    public abstract void onRemove(GameClient session, RoomItem item);
    
    /**
     * Called when an item is triggered (clicked/interacted with).
     * @param session GameClient session that triggered the item
     * @param item RoomItem that was triggered
     * @param request Request parameter (varies by item type)
     * @param userHasRights True if user has room rights
     */
    public abstract void onTrigger(GameClient session, RoomItem item, int request, boolean userHasRights);
}
