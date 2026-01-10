package com.uber.server.game.support;

/**
 * Represents a help category.
 */
public class HelpCategory {
    private final long id;
    private final String caption;
    
    public HelpCategory(long id, String caption) {
        this.id = id;
        this.caption = caption;
    }
    
    public long getCategoryId() {
        return id;
    }
    
    public String getCaption() {
        return caption;
    }
}
