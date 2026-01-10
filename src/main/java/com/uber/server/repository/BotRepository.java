package com.uber.server.repository;

import com.uber.server.storage.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for bot-related database operations.
 */
public class BotRepository {
    private static final Logger logger = LoggerFactory.getLogger(BotRepository.class);
    private final DatabasePool databasePool;
    
    public BotRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all bots from the database.
     * @return List of bot data maps
     */
    public List<Map<String, Object>> loadAllBots() {
        String sql = "SELECT * FROM bots";
        List<Map<String, Object>> bots = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> bot = new HashMap<>();
                bot.put("id", rs.getLong("id"));
                bot.put("room_id", rs.getLong("room_id"));
                bot.put("ai_type", rs.getString("ai_type"));
                bot.put("walk_mode", rs.getString("walk_mode"));
                bot.put("name", rs.getString("name"));
                bot.put("motto", rs.getString("motto"));
                bot.put("look", rs.getString("look"));
                bot.put("x", rs.getInt("x"));
                bot.put("y", rs.getInt("y"));
                bot.put("z", rs.getInt("z"));
                bot.put("rotation", rs.getInt("rotation"));
                bot.put("min_x", rs.getInt("min_x"));
                bot.put("min_y", rs.getInt("min_y"));
                bot.put("max_x", rs.getInt("max_x"));
                bot.put("max_y", rs.getInt("max_y"));
                bots.add(bot);
            }
        } catch (SQLException e) {
            logger.error("Failed to load bots: {}", e.getMessage(), e);
        }
        
        return bots;
    }
    
    /**
     * Loads random speech entries for a bot.
     * @param botId Bot ID
     * @return List of speech data (text, shout)
     */
    public List<Map<String, Object>> loadRandomSpeech(long botId) {
        String sql = "SELECT * FROM bots_speech WHERE bot_id = ?";
        List<Map<String, Object>> speeches = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, botId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> speech = new HashMap<>();
                    speech.put("text", rs.getString("text"));
                    speech.put("shout", rs.getString("shout").equals("1"));
                    speeches.add(speech);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load random speech for bot {}: {}", botId, e.getMessage(), e);
        }
        
        return speeches;
    }
    
    /**
     * Loads bot responses for a bot.
     * @param botId Bot ID
     * @return List of response data (id, bot_id, keywords, response_text, mode, serve_id)
     */
    public List<Map<String, Object>> loadResponses(long botId) {
        String sql = "SELECT * FROM bots_responses WHERE bot_id = ?";
        List<Map<String, Object>> responses = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, botId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", rs.getLong("id"));
                    response.put("bot_id", rs.getLong("bot_id"));
                    response.put("keywords", rs.getString("keywords"));
                    response.put("response_text", rs.getString("response_text"));
                    response.put("mode", rs.getString("mode"));
                    response.put("serve_id", rs.getInt("serve_id"));
                    responses.add(response);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load responses for bot {}: {}", botId, e.getMessage(), e);
        }
        
        return responses;
    }
}
