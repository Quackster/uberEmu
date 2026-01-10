package com.uber.server.game.items.interactors;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;

/**
 * Interactor for Habbo wheel items.
 */
public class InteractorHabboWheel extends FurniInteractor {
    @Override
    public void onPlace(GameClient session, RoomItem item) {
        item.setExtraData("-1");
        item.reqUpdate(10);
    }
    
    @Override
    public void onRemove(GameClient session, RoomItem item) {
        item.setExtraData("-1");
    }
    
    @Override
    public void onTrigger(GameClient session, RoomItem item, int request, boolean userHasRights) {
        if (!userHasRights) {
            return;
        }
        
        if (!"-1".equals(item.getExtraData())) {
            item.setExtraData("-1");
            item.updateState(false, true);
            item.reqUpdate(10);
        }
    }
}
