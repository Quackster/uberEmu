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
 * Repository for ecotron reward database operations.
 */
public class EcotronRepository {
    private static final Logger logger = LoggerFactory.getLogger(EcotronRepository.class);
    private final DatabasePool databasePool;
    
    public EcotronRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all ecotron rewards.
     * @return List of ecotron reward data
     */
    public List<Map<String, Object>> loadEcotronRewards() {
        String sql = "SELECT * FROM ecotron_rewards ORDER BY item_id";
        List<Map<String, Object>> rewards = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> reward = new HashMap<>();
                reward.put("id", rs.getLong("id"));
                reward.put("display_id", rs.getLong("display_id"));
                reward.put("item_id", rs.getLong("item_id"));
                reward.put("reward_level", rs.getLong("reward_level"));
                rewards.add(reward);
            }
        } catch (SQLException e) {
            logger.error("Failed to load ecotron rewards: {}", e.getMessage(), e);
        }
        
        return rewards;
    }
}
