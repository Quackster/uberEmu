package com.uber.server.game.rooms.mapping;

import com.uber.server.game.items.Item;
import com.uber.server.game.items.RoomItem;
import com.uber.server.game.pathfinding.Coord;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles room collision mapping and pathfinding data structures.
 * Manages matrices for walkability, user positions, bed positions, and heights.
 */
public class RoomMapping {
    private static final Logger logger = LoggerFactory.getLogger(RoomMapping.class);
    
    private final Room room;
    private final RoomModel model;
    
    // Collision matrices
    private MatrixState[][] matrix;
    private boolean[][] userMatrix;
    private Coord[][] bedMatrix;
    private double[][] heightMatrix;
    private double[][] topStackHeight;
    
    private final int mapSizeX;
    private final int mapSizeY;
    private final boolean allowWalkthrough;
    
    public RoomMapping(Room room, RoomModel model, boolean allowWalkthrough) {
        this.room = room;
        this.model = model;
        this.allowWalkthrough = allowWalkthrough;
        this.mapSizeX = model != null ? model.getMapSizeX() : 0;
        this.mapSizeY = model != null ? model.getMapSizeY() : 0;
        
        // Initialize matrices
        this.matrix = new MatrixState[mapSizeX][mapSizeY];
        this.userMatrix = new boolean[mapSizeX][mapSizeY];
        this.bedMatrix = new Coord[mapSizeX][mapSizeY];
        this.heightMatrix = new double[mapSizeX][mapSizeY];
        this.topStackHeight = new double[mapSizeX][mapSizeY];
        
        // Initialize bed matrix with default coords
        for (int x = 0; x < mapSizeX; x++) {
            for (int y = 0; y < mapSizeY; y++) {
                bedMatrix[x][y] = new Coord(x, y);
            }
        }
    }
    
    /**
     * Regenerates the collision matrix from heightmap and items.
     */
    public void regenerateMatrix() {
        if (model == null) {
            return;
        }
        
        // Create matrix arrays
        this.matrix = new MatrixState[mapSizeX][mapSizeY];
        this.bedMatrix = new Coord[mapSizeX][mapSizeY];
        this.heightMatrix = new double[mapSizeX][mapSizeY];
        this.topStackHeight = new double[mapSizeX][mapSizeY];
        
        // Fill in the basic data based purely on the heightmap
        for (int y = 0; y < mapSizeY; y++) {
            for (int x = 0; x < mapSizeX; x++) {
                matrix[x][y] = MatrixState.BLOCKED;
                bedMatrix[x][y] = new Coord(x, y);
                heightMatrix[x][y] = 0;
                topStackHeight[x][y] = 0.0;
                
                if (x == model.getDoorX() && y == model.getDoorY()) {
                    matrix[x][y] = MatrixState.WALKABLE_LASTSTEP;
                } else if (model.getSqState()[x][y] == SquareState.OPEN) {
                    matrix[x][y] = MatrixState.WALKABLE;
                } else if (model.getSqState()[x][y] == SquareState.SEAT) {
                    matrix[x][y] = MatrixState.WALKABLE_LASTSTEP;
                }
            }
        }
        
        // Loop through the items in the room
        List<RoomItem> floorItems = room.getFloorItems();
        for (RoomItem item : floorItems) {
            Item baseItem = item.getBaseItem();
            if (baseItem == null) {
                continue;
            }
            
            // If this is a rug, ignore it
            if (baseItem.getHeight() <= 0) {
                continue;
            }
            
            int itemX = item.getX();
            int itemY = item.getY();
            
            // Bounds check
            if (itemX < 0 || itemX >= mapSizeX || itemY < 0 || itemY >= mapSizeY) {
                continue;
            }
            
            // Make sure we're the highest item here!
            if (topStackHeight[itemX][itemY] <= item.getZ()) {
                topStackHeight[itemX][itemY] = item.getZ();
                
                // If this item is walkable and on the floor, allow users to walk here
                if (baseItem.isWalkable()) {
                    matrix[itemX][itemY] = MatrixState.WALKABLE;
                    heightMatrix[itemX][itemY] = baseItem.getHeight();
                }
                // If this item is a gate, open, and on the floor, allow users to walk here
                else if (item.getZ() <= (model.getSqFloorHeight()[itemX][itemY] + 0.1) && 
                         "gate".equalsIgnoreCase(baseItem.getInteractionType()) && 
                         "1".equals(item.getExtraData())) {
                    matrix[itemX][itemY] = MatrixState.WALKABLE;
                }
                // If this item is a seat or a bed, make it's square walkable (but only if last step)
                else if (baseItem.canSit() || "bed".equalsIgnoreCase(baseItem.getInteractionType())) {
                    matrix[itemX][itemY] = MatrixState.WALKABLE_LASTSTEP;
                }
                // Finally, if it's none of those, block the square
                else {
                    matrix[itemX][itemY] = MatrixState.BLOCKED;
                }
            }
            
            // Get affected tiles for multi-tile items
            Map<Integer, AffectedTile> points = getAffectedTiles(
                baseItem.getLength(), baseItem.getWidth(), itemX, itemY, item.getRot());
            
            if (points == null) {
                points = new HashMap<>();
            }
            
            for (AffectedTile tile : points.values()) {
                int tileX = tile.getX();
                int tileY = tile.getY();
                
                // Bounds check
                if (tileX < 0 || tileX >= mapSizeX || tileY < 0 || tileY >= mapSizeY) {
                    continue;
                }
                
                // Make sure we're the highest item here!
                if (topStackHeight[tileX][tileY] <= item.getZ()) {
                    topStackHeight[tileX][tileY] = item.getZ();
                    
                    // If this item is walkable and on the floor, allow users to walk here
                    if (baseItem.isWalkable()) {
                        matrix[tileX][tileY] = MatrixState.WALKABLE;
                        heightMatrix[tileX][tileY] = baseItem.getHeight();
                    }
                    // If this item is a gate, open, and on the floor, allow users to walk here
                    else if (item.getZ() <= (model.getSqFloorHeight()[tileX][tileY] + 0.1) && 
                             "gate".equalsIgnoreCase(baseItem.getInteractionType()) && 
                             "1".equals(item.getExtraData())) {
                        matrix[tileX][tileY] = MatrixState.WALKABLE;
                    }
                    // If this item is a seat or a bed, make it's square walkable (but only if last step)
                    else if (baseItem.canSit() || "bed".equalsIgnoreCase(baseItem.getInteractionType())) {
                        matrix[tileX][tileY] = MatrixState.WALKABLE_LASTSTEP;
                    }
                    // Finally, if it's none of those, block the square
                    else {
                        matrix[tileX][tileY] = MatrixState.BLOCKED;
                    }
                }
                
                // Set bed maps
                if ("bed".equalsIgnoreCase(baseItem.getInteractionType())) {
                    if (item.getRot() == 0 || item.getRot() == 4) {
                        bedMatrix[tileX][tileY] = new Coord(bedMatrix[tileX][tileY].getX(), itemY);
                    }
                    
                    if (item.getRot() == 2 || item.getRot() == 6) {
                        bedMatrix[tileX][tileY] = new Coord(itemX, bedMatrix[tileX][tileY].getY());
                    }
                }
            }
        }
    }
    
    /**
     * Gets the tiles affected by an item placement.
     */
    public Map<Integer, AffectedTile> getAffectedTiles(int length, int width, int posX, int posY, int rotation) {
        Map<Integer, AffectedTile> pointList = new HashMap<>();
        int index = 0;
        
        if (length > 1) {
            if (rotation == 0 || rotation == 4) {
                for (int i = 1; i < length; i++) {
                    pointList.put(index++, new AffectedTile(posX, posY + i, i));
                    
                    for (int j = 1; j < width; j++) {
                        pointList.put(index++, new AffectedTile(posX + j, posY + i, (i < j) ? j : i));
                    }
                }
            } else if (rotation == 2 || rotation == 6) {
                for (int i = 1; i < length; i++) {
                    pointList.put(index++, new AffectedTile(posX + i, posY, i));
                    
                    for (int j = 1; j < width; j++) {
                        pointList.put(index++, new AffectedTile(posX + i, posY + j, (i < j) ? j : i));
                    }
                }
            }
        }
        
        if (width > 1) {
            if (rotation == 0 || rotation == 4) {
                for (int i = 1; i < width; i++) {
                    pointList.put(index++, new AffectedTile(posX + i, posY, i));
                    
                    for (int j = 1; j < length; j++) {
                        pointList.put(index++, new AffectedTile(posX + i, posY + j, (i < j) ? j : i));
                    }
                }
            } else if (rotation == 2 || rotation == 6) {
                for (int i = 1; i < width; i++) {
                    pointList.put(index++, new AffectedTile(posX, posY + i, i));
                    
                    for (int j = 1; j < length; j++) {
                        pointList.put(index++, new AffectedTile(posX + j, posY + i, (i < j) ? j : i));
                    }
                }
            }
        }
        
        return pointList;
    }
    
    /**
     * Checks if a position is walkable.
     */
    public boolean canWalk(int x, int y, double z, boolean lastStep) {
        if (x < 0 || x >= mapSizeX || y < 0 || y >= mapSizeY) {
            return false;
        }
        
        if (squareHasUsers(x, y, lastStep)) {
            return false;
        }
        
        if (matrix[x][y] == MatrixState.BLOCKED) {
            return false;
        } else if (matrix[x][y] == MatrixState.WALKABLE_LASTSTEP && !lastStep) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if a square has users.
     */
    public boolean squareHasUsers(int x, int y, boolean lastStep) {
        if (allowWalkthrough && !lastStep) {
            return false;
        }
        
        return squareHasUsers(x, y);
    }
    
    /**
     * Checks if a square has users (using BedMatrix for resolution).
     */
    public boolean squareHasUsers(int x, int y) {
        if (x < 0 || x >= mapSizeX || y < 0 || y >= mapSizeY) {
            return false;
        }
        
        Coord coord = bedMatrix[x][y];
        if (coord.getX() < 0 || coord.getX() >= mapSizeX || 
            coord.getY() < 0 || coord.getY() >= mapSizeY) {
            return false;
        }
        
        return userMatrix[coord.getX()][coord.getY()];
    }
    
    /**
     * Regenerates the user matrix based on current user positions.
     */
    public void regenerateUserMatrix() {
        this.userMatrix = new boolean[mapSizeX][mapSizeY];
        
        List<com.uber.server.game.rooms.RoomUser> users = new ArrayList<>(room.getUsers().values());
        for (com.uber.server.game.rooms.RoomUser user : users) {
            int x = user.getX();
            int y = user.getY();
            if (x >= 0 && x < mapSizeX && y >= 0 && y < mapSizeY) {
                this.userMatrix[x][y] = true;
            }
        }
    }
    
    /**
     * Sets a user position in the user matrix.
     */
    public void setUserPosition(int x, int y, boolean occupied) {
        if (x >= 0 && x < mapSizeX && y >= 0 && y < mapSizeY) {
            userMatrix[x][y] = occupied;
        }
    }
    
    /**
     * Calculates the absolute height at a position (floor + items).
     */
    public double sqAbsoluteHeight(int x, int y) {
        if (x < 0 || x >= mapSizeX || y < 0 || y >= mapSizeY || model == null) {
            return 0.0;
        }
        
        List<RoomItem> itemsOnSquare = room.getFloorItems();
        double highestStack = 0.0;
        boolean deduct = false;
        double deductable = 0.0;
        
        // Find highest item stack on this square
        // Check if item is at position OR if any affected tile matches
        for (RoomItem item : itemsOnSquare) {
            // Check if item is on this square (including multi-tile items)
            boolean onSquare = false;
            
            // First check if item itself is at this position
            if (item.getX() == x && item.getY() == y) {
                onSquare = true;
            }
            
            // Also check affected tiles (item can occupy multiple positions)
            Item baseItem = item.getBaseItem();
            if (baseItem != null && !onSquare) {
                Map<Integer, AffectedTile> affectedTiles = getAffectedTiles(
                    baseItem.getLength(), baseItem.getWidth(), item.getX(), item.getY(), item.getRot());
                for (AffectedTile tile : affectedTiles.values()) {
                    if (tile.getX() == x && tile.getY() == y) {
                        onSquare = true;
                        break;
                    }
                }
            }
            
            if (onSquare) {
                double totalHeight = item.getTotalHeight();
                if (totalHeight > highestStack) {
                    baseItem = item.getBaseItem();
                    if (baseItem != null) {
                        if (baseItem.canSit() || "bed".equalsIgnoreCase(baseItem.getInteractionType())) {
                            deduct = true;
                            deductable = baseItem.getHeight();
                        } else {
                            deduct = false;
                        }
                    }
                    highestStack = totalHeight;
                }
            }
        }
        
        double floorHeight = model.getSqFloorHeight()[x][y];
        double stackHeight = highestStack - floorHeight;
        
        if (deduct) {
            stackHeight -= deductable;
        }
        
        if (stackHeight < 0) {
            stackHeight = 0;
        }
        
        return floorHeight + stackHeight;
    }
    
    // Getters
    public MatrixState[][] getMatrix() {
        return matrix;
    }
    
    public boolean[][] getUserMatrix() {
        return userMatrix;
    }
    
    public Coord[][] getBedMatrix() {
        return bedMatrix;
    }
    
    public double[][] getHeightMatrix() {
        return heightMatrix;
    }
    
    public double[][] getTopStackHeight() {
        return topStackHeight;
    }
    
    public int getMapSizeX() {
        return mapSizeX;
    }
    
    public int getMapSizeY() {
        return mapSizeY;
    }
}
