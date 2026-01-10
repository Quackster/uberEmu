package com.uber.server.game.rooms.mapping;

/**
 * Represents the walkability state of a tile in the room collision matrix.
 */
public enum MatrixState {
    BLOCKED(0),
    WALKABLE(1),
    WALKABLE_LASTSTEP(2);
    
    private final int value;
    
    MatrixState(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}
