package com.uber.server.game.items.interactors;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomUser;

/**
 * Interactor for dice items.
 */
public class InteractorDice extends FurniInteractor {
    @Override
    public void onPlace(GameClient session, RoomItem item) {
        // Dice doesn't do anything special on place
    }
    
    @Override
    public void onRemove(GameClient session, RoomItem item) {
        // Dice doesn't do anything special on remove
    }
    
    @Override
    public void onTrigger(GameClient session, RoomItem item, int request, boolean userHasRights) {
        Room room = item.getRoom();
        if (room == null || session == null || session.getHabbo() == null) {
            return;
        }
        
        RoomUser user = room.getRoomUserByHabbo(session.getHabbo().getId());
        if (user == null) {
            return;
        }
        
        if (room.tilesTouching(item.getX(), item.getY(), user.getX(), user.getY())) {
            if (!"-1".equals(item.getExtraData())) {
                if (request == -1) {
                    item.setExtraData("0");
                    item.updateState(false, true);
                } else {
                    item.setExtraData("-1");
                    item.updateState(false, true);
                    item.reqUpdate(4);
                }
            }
        } else {
            user.moveTo(item.getSquareInFront().getX(), item.getSquareInFront().getY());
        }
    }
}
