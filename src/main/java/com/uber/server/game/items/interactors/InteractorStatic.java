package com.uber.server.game.items.interactors;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;

/**
 * Static interactor for items that don't have special interactions.
 */
public class InteractorStatic extends FurniInteractor {
    @Override
    public void onPlace(GameClient session, RoomItem item) {
        // Static items do nothing on place
    }
    
    @Override
    public void onRemove(GameClient session, RoomItem item) {
        // Static items do nothing on remove
    }
    
    @Override
    public void onTrigger(GameClient session, RoomItem item, int request, boolean userHasRights) {
        // Static items do nothing on trigger
    }
}
