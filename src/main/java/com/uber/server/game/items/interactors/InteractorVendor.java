package com.uber.server.game.items.interactors;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomUser;
import com.uber.server.game.pathfinding.Rotation;

import java.util.Random;

/**
 * Interactor for vending machine items.
 */
public class InteractorVendor extends FurniInteractor {
    private static final Random random = new Random();
    
    @Override
    public void onPlace(GameClient session, RoomItem item) {
        item.setExtraData("0");
        
        if (item.getInteractingUser() > 0) {
            Room room = item.getRoom();
            if (room != null) {
                RoomUser user = room.getRoomUserByHabbo(item.getInteractingUser());
                if (user != null) {
                    user.setCanWalk(true);
                }
            }
        }
    }
    
    @Override
    public void onRemove(GameClient session, RoomItem item) {
        item.setExtraData("0");
        
        if (item.getInteractingUser() > 0) {
            Room room = item.getRoom();
            if (room != null) {
                RoomUser user = room.getRoomUserByHabbo(item.getInteractingUser());
                if (user != null) {
                    user.setCanWalk(true);
                }
            }
        }
    }
    
    @Override
    public void onTrigger(GameClient session, RoomItem item, int request, boolean userHasRights) {
        Room room = item.getRoom();
        if (room == null || session == null || session.getHabbo() == null) {
            return;
        }
        
        com.uber.server.game.items.Item baseItem = item.getBaseItem();
        if (baseItem == null) {
            return;
        }
        
        String vendingIds = baseItem.getVendingIds();
        if (vendingIds == null || vendingIds.isEmpty()) {
            return;
        }
        
        if (!"1".equals(item.getExtraData()) && item.getInteractingUser() == 0) {
            RoomUser user = room.getRoomUserByHabbo(session.getHabbo().getId());
            if (user == null) {
                return;
            }
            
            if (!room.tilesTouching(user.getX(), user.getY(), item.getX(), item.getY())) {
                user.moveTo(item.getSquareInFront().getX(), item.getSquareInFront().getY());
                return;
            }
            
            item.setInteractingUser(session.getHabbo().getId());
            user.setCanWalk(false);
            user.clearMovement(true);
            user.setRot(Rotation.calculate(user.getX(), user.getY(), item.getX(), item.getY()));
            
            item.reqUpdate(2);
            item.setExtraData("1");
            item.updateState(false, true);
        }
    }
}
