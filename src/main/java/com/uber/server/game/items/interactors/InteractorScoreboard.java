package com.uber.server.game.items.interactors;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;

/**
 * Interactor for scoreboard items.
 */
public class InteractorScoreboard extends FurniInteractor {
    @Override
    public void onPlace(GameClient session, RoomItem item) {
        // Scoreboard doesn't do anything special on place
    }
    
    @Override
    public void onRemove(GameClient session, RoomItem item) {
        // Scoreboard doesn't do anything special on remove
    }
    
    @Override
    public void onTrigger(GameClient session, RoomItem item, int request, boolean userHasRights) {
        if (!userHasRights) {
            return;
        }
        
        int newMode = 0;
        try {
            newMode = Integer.parseInt(item.getExtraData());
        } catch (NumberFormatException e) {
            // Invalid extra data, default to 0
        }
        
        if (request == 0) {
            // Toggle between -1 and 0
            if (newMode <= -1) {
                newMode = 0;
            } else if (newMode >= 0) {
                newMode = -1;
            }
        } else if (request >= 1) {
            if (request == 1) {
                // Decrement
                newMode--;
                if (newMode < 0) {
                    newMode = 0;
                }
            } else if (request == 2) {
                // Increment
                newMode++;
                if (newMode >= 100) {
                    newMode = 0;
                }
            }
        }
        
        item.setExtraData(String.valueOf(newMode));
        item.updateState(false, true);
    }
}
