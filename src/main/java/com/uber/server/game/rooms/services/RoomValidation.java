package com.uber.server.game.rooms.services;

import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomModel;
import com.uber.server.game.rooms.RoomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for room validation helpers.
 * Handles walkability checks, position validation, and tile operations.
 */
public class RoomValidation {
    private static final Logger logger = LoggerFactory.getLogger(RoomValidation.class);
    
    private final Room room;
    private final ConcurrentHashMap<Long, RoomUser> users;
    
    public RoomValidation(Room room, ConcurrentHashMap<Long, RoomUser> users) {
        this.room = room;
        this.users = users;
    }
    
    /**
     * Checks if a position is walkable.
     * Uses RoomMapping if available, otherwise falls back to simplified check.
     */
    public boolean canWalk(int x, int y, double z, boolean lastStep) {
        // Use RoomMapping if available
        if (room.getRoomMapping() != null) {
            return room.getRoomMapping().canWalk(x, y, z, lastStep);
        }
        
        // Fallback to simplified check
        RoomModel model = room.getModel();
        if (model == null) {
            return false;
        }
        
        // Basic bounds check
        if (x < 0 || y < 0 || x >= model.getMapSizeX() || y >= model.getMapSizeY()) {
            return false;
        }
        
        // Check if square has users
        for (RoomUser user : users.values()) {
            if (user.getX() == x && user.getY() == y && !user.isSpectator()) {
                return false;
            }
        }
        
        // For now, allow walking (simplified - full implementation uses Matrix)
        return true;
    }
    
    /**
     * Checks if a square has users (excluding last step positions).
     */
    public boolean squareHasUsers(int x, int y, boolean lastStep) {
        // Use RoomMapping if available
        if (room.getRoomMapping() != null) {
            return room.getRoomMapping().squareHasUsers(x, y, lastStep);
        }
        
        return squareHasUsers(x, y);
    }
    
    /**
     * Checks if a square has users.
     */
    public boolean squareHasUsers(int x, int y) {
        // Use RoomMapping if available
        if (room.getRoomMapping() != null) {
            return room.getRoomMapping().squareHasUsers(x, y);
        }
        
        // Fallback to simple check
        for (RoomUser user : users.values()) {
            if (user.isSpectator()) {
                continue;
            }
            if (user.getX() == x && user.getY() == y) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if two tiles are touching (adjacent or same).
     */
    public boolean tilesTouching(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return dx <= 1 && dy <= 1;
    }
    
    /**
     * Calculates tile distance between two coordinates.
     */
    public int tileDistance(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return Math.max(dx, dy);
    }
    
    /**
     * Validates and normalizes wall position string.
     */
    public String wallPositionCheck(String wallPosition) {
        if (wallPosition == null || wallPosition.isEmpty()) {
            return null;
        }
        
        try {
            // Check for invalid characters
            if (wallPosition.contains("\r") || wallPosition.contains("\t")) {
                return null;
            }
            
            String[] posD = wallPosition.split(" ");
            if (posD.length < 3) {
                return null;
            }
            
            if (!"l".equals(posD[2]) && !"r".equals(posD[2])) {
                return null;
            }
            
            // Parse width
            if (!posD[0].startsWith(":w=")) {
                return null;
            }
            String[] widD = posD[0].substring(3).split(",");
            if (widD.length != 2) {
                return null;
            }
            int widthX = Integer.parseInt(widD[0]);
            int widthY = Integer.parseInt(widD[1]);
            if (widthX < 0 || widthY < 0 || widthX > 200 || widthY > 200) {
                return null;
            }
            
            // Parse length
            if (!posD[1].startsWith("l=")) {
                return null;
            }
            String[] lenD = posD[1].substring(2).split(",");
            if (lenD.length != 2) {
                return null;
            }
            int lengthX = Integer.parseInt(lenD[0]);
            int lengthY = Integer.parseInt(lenD[1]);
            if (lengthX < 0 || lengthY < 0 || lengthX > 200 || lengthY > 200) {
                return null;
            }
            
            return ":w=" + widthX + "," + widthY + " l=" + lengthX + "," + lengthY + " " + posD[2];
        } catch (Exception e) {
            logger.warn("Invalid wall position format: {}", wallPosition);
            return null;
        }
    }
    
    /**
     * Regenerates the user matrix based on current user positions.
     */
    public void regenerateUserMatrix() {
        // Use RoomMapping if available
        if (room.getRoomMapping() != null) {
            room.getRoomMapping().regenerateUserMatrix();
        }
    }
}
