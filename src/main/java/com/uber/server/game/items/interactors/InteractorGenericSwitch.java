package com.uber.server.game.items.interactors;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;

/**
 * Interactor for generic switch items with multiple modes.
 */
public class InteractorGenericSwitch extends FurniInteractor {
    private final int modes;
    
    public InteractorGenericSwitch(int modes) {
        this.modes = Math.max(0, modes - 1);
    }
    
    @Override
    public void onPlace(GameClient session, RoomItem item) {
        // Generic switch doesn't do anything special on place
    }
    
    @Override
    public void onRemove(GameClient session, RoomItem item) {
        // Generic switch doesn't do anything special on remove
    }
    
    @Override
    public void onTrigger(GameClient session, RoomItem item, int request, boolean userHasRights) {
        if (!userHasRights) {
            return;
        }
        
        if (modes == 0) {
            return;
        }
        
        int currentMode = 0;
        try {
            currentMode = Integer.parseInt(item.getExtraData());
        } catch (NumberFormatException e) {
            // Invalid extra data, default to 0
        }
        
        int newMode;
        if (currentMode <= 0) {
            newMode = 1;
        } else if (currentMode >= modes) {
            newMode = 0;
        } else {
            newMode = currentMode + 1;
        }
        
        item.setExtraData(String.valueOf(newMode));
        item.updateState(false, true);
    }
}
