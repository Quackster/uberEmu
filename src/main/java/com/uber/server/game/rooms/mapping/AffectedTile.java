package com.uber.server.game.rooms.mapping;

import com.uber.server.game.pathfinding.Coord;

/**
 * Represents a tile affected by an item placement.
 */
public class AffectedTile {
    private final int x;
    private final int y;
    private final int i;
    
    public AffectedTile(int x, int y, int i) {
        this.x = x;
        this.y = y;
        this.i = i;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getI() {
        return i;
    }
    
    public Coord toCoord() {
        return new Coord(x, y);
    }
}
