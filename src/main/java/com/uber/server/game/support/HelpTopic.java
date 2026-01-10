package com.uber.server.game.support;

/**
 * Represents a help topic.
 */
public class HelpTopic {
    private final long id;
    private final String caption;
    private final String body;
    private final long categoryId;
    
    public HelpTopic(long id, String caption, String body, long categoryId) {
        this.id = id;
        this.caption = caption;
        this.body = body;
        this.categoryId = categoryId;
    }
    
    public long getTopicId() {
        return id;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public String getBody() {
        return body;
    }
    
    public long getCategoryId() {
        return categoryId;
    }
}
