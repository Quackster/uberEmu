package com.uber.server.game.bots;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bot response to user messages.
 */
public class BotResponse {
    private final long id;
    private final long botId;
    private final List<String> keywords;
    private final String responseText;
    private final String responseType; // "say", "shout", "whisper"
    private final int serveId;
    
    public BotResponse(long id, long botId, String keywords, String responseText, String responseType, int serveId) {
        this.id = id;
        this.botId = botId;
        this.keywords = new ArrayList<>();
        this.responseText = responseText;
        this.responseType = responseType != null ? responseType.toLowerCase() : "say";
        this.serveId = serveId;
        
        // Parse keywords (semicolon-separated)
        if (keywords != null && !keywords.isEmpty()) {
            String[] keywordArray = keywords.split(";");
            for (String keyword : keywordArray) {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    this.keywords.add(keyword.trim().toLowerCase());
                }
            }
        }
    }
    
    /**
     * Checks if the message matches any of the keywords.
     * @param message User message
     * @return True if message contains any keyword
     */
    public boolean keywordMatched(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        
        String lowerMessage = message.toLowerCase();
        for (String keyword : keywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    public long getId() {
        return id;
    }
    
    public long getBotId() {
        return botId;
    }
    
    public List<String> getKeywords() {
        return new ArrayList<>(keywords);
    }
    
    public String getResponseText() {
        return responseText;
    }
    
    public String getResponseType() {
        return responseType;
    }
    
    public int getServeId() {
        return serveId;
    }
}
