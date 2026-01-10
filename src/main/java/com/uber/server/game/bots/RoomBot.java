package com.uber.server.game.bots;

import com.uber.server.repository.BotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a bot in a room.
 */
public class RoomBot {
    private static final Logger logger = LoggerFactory.getLogger(RoomBot.class);
    
    private final long botId;
    private final long roomId;
    private final String aiType;
    private final String walkingMode;
    private final String name;
    private final String motto;
    private final String look;
    private final int x;
    private final int y;
    private final int z;
    private final int rot;
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;
    
    private final List<RandomSpeech> randomSpeech;
    private final List<BotResponse> responses;
    private final Random random;
    
    /**
     * Creates a RoomBot from database data.
     * @param row Bot data from database
     * @param botRepository BotRepository for loading speech/responses
     */
    public RoomBot(Map<String, Object> row, BotRepository botRepository) {
        this.botId = ((Number) row.get("id")).longValue();
        this.roomId = ((Number) row.get("room_id")).longValue();
        this.aiType = (String) row.get("ai_type");
        this.walkingMode = (String) row.get("walk_mode");
        this.name = (String) row.get("name");
        this.motto = (String) row.get("motto");
        this.look = (String) row.get("look");
        this.x = ((Number) row.get("x")).intValue();
        this.y = ((Number) row.get("y")).intValue();
        this.z = ((Number) row.get("z")).intValue();
        this.rot = ((Number) row.get("rotation")).intValue();
        this.minX = ((Number) row.get("min_x")).intValue();
        this.minY = ((Number) row.get("min_y")).intValue();
        this.maxX = ((Number) row.get("max_x")).intValue();
        this.maxY = ((Number) row.get("max_y")).intValue();
        
        this.random = new Random();
        this.randomSpeech = new ArrayList<>();
        this.responses = new ArrayList<>();
        
        // Load random speech and responses
        if (botRepository != null) {
            loadRandomSpeech(botRepository);
            loadResponses(botRepository);
        }
    }
    
    /**
     * Loads random speech entries from database.
     */
    private void loadRandomSpeech(BotRepository botRepository) {
        randomSpeech.clear();
        
        List<Map<String, Object>> speechData = botRepository.loadRandomSpeech(botId);
        for (Map<String, Object> row : speechData) {
            String text = (String) row.get("text");
            boolean shout = (Boolean) row.get("shout");
            randomSpeech.add(new RandomSpeech(text, shout));
        }
    }
    
    /**
     * Loads bot responses from database.
     */
    private void loadResponses(BotRepository botRepository) {
        responses.clear();
        
        List<Map<String, Object>> responseData = botRepository.loadResponses(botId);
        for (Map<String, Object> row : responseData) {
            long id = ((Number) row.get("id")).longValue();
            long botId = ((Number) row.get("bot_id")).longValue();
            String keywords = (String) row.get("keywords");
            String responseText = (String) row.get("response_text");
            String mode = (String) row.get("mode");
            int serveId = ((Number) row.get("serve_id")).intValue();
            
            responses.add(new BotResponse(id, botId, keywords, responseText, mode, serveId));
        }
    }
    
    /**
     * Gets a response for a user message.
     * @param message User message
     * @return BotResponse if keyword matched, null otherwise
     */
    public BotResponse getResponse(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }
        
        for (BotResponse response : responses) {
            if (response.keywordMatched(message)) {
                return response;
            }
        }
        
        return null;
    }
    
    /**
     * Gets a random speech entry.
     * @return RandomSpeech entry
     */
    public RandomSpeech getRandomSpeech() {
        if (randomSpeech.isEmpty()) {
            return null;
        }
        
        return randomSpeech.get(random.nextInt(randomSpeech.size()));
    }
    
    /**
     * Generates a BotAI instance for this bot.
     * @param virtualId Virtual ID of the RoomUser
     * @return BotAI instance
     */
    public BotAI generateBotAI(int virtualId) {
        String lowerAiType = aiType != null ? aiType.toLowerCase() : "generic";
        
        return switch (lowerAiType) {
            case "guide" -> new GuideBot();
            case "pet" -> new PetBot(virtualId);
            default -> new GenericBot(virtualId);
        };
    }
    
    public boolean isPet() {
        return "pet".equalsIgnoreCase(aiType);
    }
    
    // Getters
    public long getBotId() { return botId; }
    public long getRoomId() { return roomId; }
    public String getAiType() { return aiType; }
    public String getWalkingMode() { return walkingMode; }
    public String getName() { return name; }
    public String getMotto() { return motto; }
    public String getLook() { return look; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getRot() { return rot; }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public List<BotResponse> getResponses() { return new ArrayList<>(responses); }
}
