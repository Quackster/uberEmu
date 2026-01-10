package com.uber.server.game.bots;

import com.uber.server.repository.BotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all bots in the hotel.
 */
public class BotManager {
    private static final Logger logger = LoggerFactory.getLogger(BotManager.class);
    
    private final BotRepository botRepository;
    private final ConcurrentHashMap<Long, RoomBot> bots; // Bot ID -> RoomBot
    
    public BotManager(BotRepository botRepository) {
        this.botRepository = botRepository;
        this.bots = new ConcurrentHashMap<>();
    }
    
    /**
     * Loads all bots from the database.
     */
    public void loadBots() {
        bots.clear();
        
        List<Map<String, Object>> botData = botRepository.loadAllBots();
        for (Map<String, Object> row : botData) {
            try {
                RoomBot bot = new RoomBot(row, botRepository);
                bots.put(bot.getBotId(), bot);
            } catch (Exception e) {
                logger.error("Failed to load bot from row: {}", e.getMessage(), e);
            }
        }
        
        logger.info("Loaded {} bots", bots.size());
    }
    
    /**
     * Checks if a room has bots.
     * @param roomId Room ID
     * @return True if room has at least one bot
     */
    public boolean roomHasBots(long roomId) {
        return getBotsForRoom(roomId).size() >= 1;
    }
    
    /**
     * Gets all bots for a specific room.
     * @param roomId Room ID
     * @return List of RoomBot instances
     */
    public List<RoomBot> getBotsForRoom(long roomId) {
        List<RoomBot> roomBots = new ArrayList<>();
        
        for (RoomBot bot : bots.values()) {
            if (bot.getRoomId() == roomId) {
                roomBots.add(bot);
            }
        }
        
        return roomBots;
    }
    
    /**
     * Gets a bot by ID.
     * @param botId Bot ID
     * @return RoomBot instance, or null if not found
     */
    public RoomBot getBot(long botId) {
        return bots.get(botId);
    }
    
    /**
     * Gets all bots.
     * @return Map of all bots (Bot ID -> RoomBot)
     */
    public Map<Long, RoomBot> getAllBots() {
        return new ConcurrentHashMap<>(bots);
    }
}
