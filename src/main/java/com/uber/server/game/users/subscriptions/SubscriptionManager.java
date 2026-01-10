package com.uber.server.game.users.subscriptions;

import com.uber.server.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user subscriptions.
 */
public class SubscriptionManager {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionManager.class);
    
    private final long userId;
    private final SubscriptionRepository subscriptionRepository;
    private final ConcurrentHashMap<String, Subscription> subscriptions;
    
    public SubscriptionManager(long userId, SubscriptionRepository subscriptionRepository) {
        this.userId = userId;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptions = new ConcurrentHashMap<>();
    }
    
    /**
     * Loads subscriptions from the database.
     */
    public void loadSubscriptions() {
        subscriptions.clear();
        
        List<Map<String, Object>> subscriptionData = subscriptionRepository.loadSubscriptions(userId);
        for (Map<String, Object> row : subscriptionData) {
            try {
                String subscriptionId = (String) row.get("subscription_id");
                long timestampActivated = ((Number) row.get("timestamp_activated")).longValue();
                long timestampExpire = ((Number) row.get("timestamp_expire")).longValue();
                
                Subscription subscription = new Subscription(subscriptionId, timestampActivated, timestampExpire);
                subscriptions.put(subscriptionId.toLowerCase(), subscription);
            } catch (Exception e) {
                logger.error("Failed to load subscription: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Gets a list of subscription IDs.
     * @return List of subscription IDs
     */
    public List<String> getSubList() {
        List<String> list = new ArrayList<>();
        for (Subscription subscription : subscriptions.values()) {
            list.add(subscription.getSubscriptionId());
        }
        return list;
    }
    
    /**
     * Checks if the user has a valid subscription.
     * @param subscriptionId Subscription ID to check
     * @return True if user has a valid subscription, false otherwise
     */
    public boolean hasSubscription(String subscriptionId) {
        if (subscriptionId == null) {
            return false;
        }
        
        Subscription subscription = subscriptions.get(subscriptionId.toLowerCase());
        if (subscription == null) {
            return false;
        }
        
        return subscription.isValid();
    }
    
    /**
     * Gets a subscription by ID.
     * @param subscriptionId Subscription ID
     * @return Subscription object, or null if not found
     */
    public Subscription getSubscription(String subscriptionId) {
        if (subscriptionId == null) {
            return null;
        }
        
        return subscriptions.get(subscriptionId.toLowerCase());
    }
    
    /**
     * Adds or extends a subscription.
     * @param subscriptionId Subscription ID
     * @param durationSeconds Duration in seconds to add/extend
     */
    public void addOrExtendSubscription(String subscriptionId, int durationSeconds) {
        subscriptionId = subscriptionId.toLowerCase();
        
        Subscription subscription = subscriptions.get(subscriptionId);
        if (subscription != null) {
            // Extend existing subscription
            subscription.extendSubscription(durationSeconds);
            subscriptionRepository.updateSubscriptionExpire(userId, subscriptionId, subscription.getExpireTime());
        } else {
            // Create new subscription
            long now = com.uber.server.util.TimeUtil.getUnixTimestamp();
            long expireTime = now + durationSeconds;
            subscription = new Subscription(subscriptionId, now, expireTime);
            subscriptions.put(subscriptionId, subscription);
            subscriptionRepository.createSubscription(userId, subscriptionId, now, expireTime);
        }
    }
    
    /**
     * Clears all subscriptions from memory.
     */
    public void clear() {
        subscriptions.clear();
    }
}
