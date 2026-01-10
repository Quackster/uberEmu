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
 * Repository for advertisement database operations.
 */
public class AdvertisementRepository {
    private static final Logger logger = LoggerFactory.getLogger(AdvertisementRepository.class);
    private final DatabasePool databasePool;
    
    public AdvertisementRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all enabled room advertisements.
     * @return List of advertisement data
     */
    public List<Map<String, Object>> loadRoomAdvertisements() {
        String sql = "SELECT * FROM room_ads WHERE enabled = '1'";
        List<Map<String, Object>> ads = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> ad = new HashMap<>();
                ad.put("id", rs.getLong("id"));
                ad.put("ad_image", rs.getString("ad_image"));
                ad.put("ad_link", rs.getString("ad_link"));
                ad.put("views", rs.getInt("views"));
                ad.put("views_limit", rs.getInt("views_limit"));
                ads.add(ad);
            }
        } catch (SQLException e) {
            logger.error("Failed to load room advertisements: {}", e.getMessage(), e);
        }
        
        return ads;
    }
    
    /**
     * Increments view count for an advertisement.
     * @param adId Advertisement ID
     * @return True if update was successful
     */
    public boolean incrementViews(long adId) {
        String sql = "UPDATE room_ads SET views = views + 1 WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, adId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to increment views for ad {}: {}", adId, e.getMessage(), e);
            return false;
        }
    }
}
