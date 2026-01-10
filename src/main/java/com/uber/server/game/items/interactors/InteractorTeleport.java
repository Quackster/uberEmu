package com.uber.server.game.items.interactors;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;
import com.uber.server.game.items.TeleHandler;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomUser;
import com.uber.server.game.pathfinding.Coord;
import com.uber.server.messages.ServerMessage;

/**
 * Interactor for teleport items.
 */
public class InteractorTeleport extends FurniInteractor {
    @Override
    public void onPlace(GameClient session, RoomItem item) {
        item.setExtraData("0");
        
        Room room = item.getRoom();
        if (room == null) {
            return;
        }
        
        if (item.getInteractingUser() > 0) {
            RoomUser user = room.getRoomUserByHabbo(item.getInteractingUser());
            if (user != null) {
                user.clearMovement(true);
                user.setAllowOverride(false);
                user.setCanWalk(true);
            }
            item.setInteractingUser(0);
        }
        
        if (item.getInteractingUser2() > 0) {
            RoomUser user = room.getRoomUserByHabbo(item.getInteractingUser2());
            if (user != null) {
                user.clearMovement(true);
                user.setAllowOverride(false);
                user.setCanWalk(true);
            }
            item.setInteractingUser2(0);
        }
        
        room.regenerateUserMatrix();
    }
    
    @Override
    public void onRemove(GameClient session, RoomItem item) {
        item.setExtraData("0");
        
        Room room = item.getRoom();
        if (room == null) {
            return;
        }
        
        if (item.getInteractingUser() > 0) {
            RoomUser user = room.getRoomUserByHabbo(item.getInteractingUser());
            if (user != null) {
                user.unlockWalking();
            }
            item.setInteractingUser(0);
        }
        
        if (item.getInteractingUser2() > 0) {
            RoomUser user = room.getRoomUserByHabbo(item.getInteractingUser2());
            if (user != null) {
                user.unlockWalking();
            }
            item.setInteractingUser2(0);
        }
        
        room.regenerateUserMatrix();
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
        
        Coord itemCoord = item.getCoordinate();
        Coord squareInFront = item.getSquareInFront();
        Coord userCoord = user.getCoordinate();
        
        // Check if user is in the right position
        if (userCoord.equals(itemCoord) || userCoord.equals(squareInFront)) {
            // Check if teleport is free
            if (item.getInteractingUser() != 0) {
                return; // Teleport is busy
            }
            
            // Set teleport delay and interacting user
            user.setTeleDelay(-1);
            item.setInteractingUser(user.getHabboId());
        } else if (user.canWalk()) {
            // Move user to square in front of teleport
            user.moveTo(squareInFront.getX(), squareInFront.getY());
        }
    }
    
    /**
     * Gets the game instance from a room item.
     * Helper method to access game from item.
     */
    private com.uber.server.game.Game getGame(RoomItem item) {
        com.uber.server.game.rooms.Room room = item.getRoom();
        if (room != null) {
            return room.getGame();
        }
        return null;
    }
}
