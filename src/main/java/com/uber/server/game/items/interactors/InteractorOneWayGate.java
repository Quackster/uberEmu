package com.uber.server.game.items.interactors;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomUser;
import com.uber.server.game.pathfinding.Coord;

/**
 * Interactor for one-way gate items.
 */
public class InteractorOneWayGate extends FurniInteractor {
    @Override
    public void onPlace(GameClient session, RoomItem item) {
        item.setExtraData("0");
        
        if (item.getInteractingUser() > 0) {
            Room room = item.getRoom();
            if (room != null) {
                RoomUser user = room.getRoomUserByHabbo(item.getInteractingUser());
                if (user != null) {
                    user.clearMovement(true);
                    user.unlockWalking();
                }
            }
            item.setInteractingUser(0);
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
                    user.clearMovement(true);
                    user.unlockWalking();
                }
            }
            item.setInteractingUser(0);
        }
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
        
        Coord squareInFront = item.getSquareInFront();
        Coord userCoord = user.getCoordinate();
        
        if (!userCoord.equals(squareInFront) && user.canWalk()) {
            user.moveTo(squareInFront.getX(), squareInFront.getY());
            return;
        }
        
        Coord squareBehind = item.getSquareBehind();
        if (!room.canWalk(squareBehind.getX(), squareBehind.getY(), item.getZ(), true)) {
            return;
        }
        
        if (item.getInteractingUser() == 0) {
            item.setInteractingUser(user.getHabboId());
            user.setCanWalk(false);
            
            if (user.isWalking() && (user.getGoalX() != squareInFront.getX() || user.getGoalY() != squareInFront.getY())) {
                user.clearMovement(true);
            }
            
            user.setAllowOverride(true);
            user.moveTo(item.getX(), item.getY());
            
            item.reqUpdate(3);
        }
    }
}
