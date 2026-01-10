package com.uber.server.game.roles;

/**
 * Represents a user role/rank.
 */
public class Role {
    private final long roleId;
    private final String caption;
    
    public Role(long roleId, String caption) {
        this.roleId = roleId;
        this.caption = caption;
    }
    
    public long getRoleId() {
        return roleId;
    }
    
    public String getCaption() {
        return caption;
    }
}
