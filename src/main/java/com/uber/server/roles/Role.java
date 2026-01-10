package com.uber.server.roles;

/**
 * Represents a user role/rank.
 * Ported from HabboHotel/Roles/Role.cs
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
