package com.uber.server.game.users.subscriptions;

import com.uber.server.util.TimeUtil;

/**
 * Represents a user subscription.
 */
public class Subscription {
    private final String subscriptionId;
    private final long timestampActivated;
    private long timestampExpire;
    
    public Subscription(String subscriptionId, long timestampActivated, long timestampExpire) {
        this.subscriptionId = subscriptionId;
        this.timestampActivated = timestampActivated;
        this.timestampExpire = timestampExpire;
    }
    
    public String getSubscriptionId() {
        return subscriptionId;
    }
    
    public long getTimestampActivated() {
        return timestampActivated;
    }
    
    public long getExpireTime() {
        return timestampExpire;
    }
    
    /**
     * Checks if the subscription is still valid (not expired).
     * @return True if subscription is valid, false if expired
     */
    public boolean isValid() {
        return timestampExpire > TimeUtil.getUnixTimestamp();
    }
    
    /**
     * Extends the subscription by the given number of seconds.
     * @param seconds Number of seconds to extend
     */
    public void extendSubscription(int seconds) {
        this.timestampExpire += seconds;
    }
    
    public void setExpireTime(long timestampExpire) {
        this.timestampExpire = timestampExpire;
    }
}
