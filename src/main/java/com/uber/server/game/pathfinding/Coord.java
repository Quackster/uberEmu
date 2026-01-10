package com.uber.server.game.pathfinding;

import java.util.Objects;

/**
 * Represents a coordinate (x, y) in the room grid.
 */
public class Coord {
    private int x;
    private int y;
    
    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Coord coord = (Coord) obj;
        return x == coord.x && y == coord.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    
    @Override
    public String toString() {
        return "Coord{x=" + x + ", y=" + y + "}";
    }
}
