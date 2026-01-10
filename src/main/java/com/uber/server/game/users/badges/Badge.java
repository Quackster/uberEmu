package com.uber.server.game.users.badges;

/**
 * Represents a user badge.
 */
public class Badge {
    private final String code;
    private int slot;
    
    public Badge(String code, int slot) {
        this.code = code;
        this.slot = slot;
    }
    
    public String getCode() {
        return code;
    }
    
    public int getSlot() {
        return slot;
    }
    
    public void setSlot(int slot) {
        this.slot = slot;
    }
}
