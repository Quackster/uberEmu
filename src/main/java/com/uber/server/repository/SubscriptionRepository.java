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
 * Repository for subscription database operations.
 */
public class SubscriptionRepository {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionRepository.class);
    private final DatabasePool databasePool;
    
    public SubscriptionRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads all subscriptions for a user.
     * @param userId User ID
     * @return List of subscription data
     */
    public List<Map<String, Object>> loadSubscriptions(long userId) {
        String sql = "SELECT * FROM user_subscriptions WHERE user_id = ?";
        List<Map<String, Object>> subscriptions = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> sub = new HashMap<>();
                    sub.put("subscription_id", rs.getString("subscription_id"));
                    sub.put("timestamp_activated", rs.getLong("timestamp_activated"));
                    sub.put("timestamp_expire", rs.getLong("timestamp_expire"));
                    subscriptions.add(sub);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load subscriptions for user {}: {}", userId, e.getMessage(), e);
        }
        
        return subscriptions;
    }
    
    /**
     * Creates a new subscription.
     * @param userId User ID
     * @param subscriptionId Subscription ID
     * @param timestampActivated Activation timestamp
     * @param timestampExpire Expiration timestamp
     * @return True if creation was successful
     */
    public boolean createSubscription(long userId, String subscriptionId, long timestampActivated, long timestampExpire) {
        String sql = "INSERT INTO user_subscriptions (user_id, subscription_id, timestamp_activated, timestamp_expire) " +
                    "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setString(2, subscriptionId);
            stmt.setLong(3, timestampActivated);
            stmt.setLong(4, timestampExpire);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to create subscription: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates subscription expiration time.
     * @param userId User ID
     * @param subscriptionId Subscription ID
     * @param timestampExpire New expiration timestamp
     * @return True if update was successful
     */
    public boolean updateSubscriptionExpire(long userId, String subscriptionId, long timestampExpire) {
        String sql = "UPDATE user_subscriptions SET timestamp_expire = ? WHERE user_id = ? AND subscription_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, timestampExpire);
            stmt.setLong(2, userId);
            stmt.setString(3, subscriptionId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update subscription expiration: {}", e.getMessage(), e);
            return false;
        }
    }
}
