package com.uber.server.game.rooms.mapping;

/**
 * Represents the state of a square in the room heightmap.
 */
public enum SquareState {
    OPEN(0),
    BLOCKED(1),
    SEAT(2);
    
    private final int value;
    
    SquareState(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}
