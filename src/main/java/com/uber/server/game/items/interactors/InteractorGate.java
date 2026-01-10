package com.uber.server.game.items.interactors;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;
import com.uber.server.game.rooms.Room;

/**
 * Interactor for gate items with multiple modes.
 */
public class InteractorGate extends FurniInteractor {
    private final int modes;
    
    public InteractorGate(int modes) {
        this.modes = Math.max(0, modes - 1);
    }
    
    @Override
    public void onPlace(GameClient session, RoomItem item) {
        // Gate doesn't do anything special on place
    }
    
    @Override
    public void onRemove(GameClient session, RoomItem item) {
        // Gate doesn't do anything special on remove
    }
    
    @Override
    public void onTrigger(GameClient session, RoomItem item, int request, boolean userHasRights) {
        if (!userHasRights) {
            return;
        }
        
        Room room = item.getRoom();
        if (room == null) {
            return;
        }
        
        if (modes == 0) {
            item.updateState(false, true);
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
        
        // If closing gate (newMode == 0), check if there are users on the gate
        if (newMode == 0) {
            if (room.squareHasUsers(item.getX(), item.getY())) {
                return; // Can't close if users are on it
            }
            
            // Check affected tiles (simplified - would use GetAffectedTiles in full implementation)
            // For now, just check the main tile
        }
        
        item.setExtraData(String.valueOf(newMode));
        item.updateState(false, true);
        // Note: GenerateMaps() would be called here in full implementation
    }
}
