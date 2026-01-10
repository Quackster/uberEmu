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
 * Repository for help system database operations.
 */
public class HelpRepository {
    private static final Logger logger = LoggerFactory.getLogger(HelpRepository.class);
    private final DatabasePool databasePool;
    
    public HelpRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all help subjects/categories.
     * @return List of category data (id, caption)
     */
    public List<Map<String, Object>> loadCategories() {
        String sql = "SELECT * FROM help_subjects";
        List<Map<String, Object>> categories = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> category = new HashMap<>();
                category.put("id", rs.getLong("id"));
                category.put("caption", rs.getString("caption"));
                categories.add(category);
            }
        } catch (SQLException e) {
            logger.error("Failed to load help categories: {}", e.getMessage(), e);
        }
        
        return categories;
    }
    
    /**
     * Loads all help topics.
     * @return List of topic data (id, title, body, subject, known_issue)
     */
    public List<Map<String, Object>> loadTopics() {
        String sql = "SELECT * FROM help_topics";
        List<Map<String, Object>> topics = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> topic = new HashMap<>();
                topic.put("id", rs.getLong("id"));
                topic.put("title", rs.getString("title"));
                topic.put("body", rs.getString("body"));
                topic.put("subject", rs.getLong("subject"));
                topic.put("known_issue", rs.getInt("known_issue"));
                topics.add(topic);
            }
        } catch (SQLException e) {
            logger.error("Failed to load help topics: {}", e.getMessage(), e);
        }
        
        return topics;
    }
    
    /**
     * Searches for help topics matching the query.
     * @param query Search query (will be wrapped with % for LIKE)
     * @return List of matching topics (id, title)
     */
    public List<Map<String, Object>> searchTopics(String query) {
        String sql = "SELECT id, title FROM help_topics WHERE title LIKE ? OR body LIKE ? LIMIT 25";
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + query + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> topic = new HashMap<>();
                    topic.put("id", rs.getLong("id"));
                    topic.put("title", rs.getString("title"));
                    results.add(topic);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to search help topics: {}", e.getMessage(), e);
        }
        
        return results;
    }
}
