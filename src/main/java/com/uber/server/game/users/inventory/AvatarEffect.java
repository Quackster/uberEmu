package com.uber.server.game.users.inventory;

import com.uber.server.util.TimeUtil;

/**
 * Represents an avatar effect.
 */
public class AvatarEffect {
    private final int effectId;
    private final int totalDuration;
    private boolean activated;
    private long stampActivated;
    
    public AvatarEffect(int effectId, int totalDuration, boolean activated, long activateTimestamp) {
        this.effectId = effectId;
        this.totalDuration = totalDuration;
        this.activated = activated;
        this.stampActivated = activateTimestamp;
    }
    
    public int getEffectId() {
        return effectId;
    }
    
    public int getTotalDuration() {
        return totalDuration;
    }
    
    public boolean isActivated() {
        return activated;
    }
    
    public long getStampActivated() {
        return stampActivated;
    }
    
    /**
     * Gets the time left for this effect in seconds.
     * @return Time left in seconds, or -1 if not activated
     */
    public int getTimeLeft() {
        if (!activated) {
            return -1;
        }
        
        long diff = TimeUtil.getUnixTimestamp() - stampActivated;
        
        if (diff >= totalDuration) {
            return 0;
        }
        
        return (int) (totalDuration - diff);
    }
    
    /**
     * Checks if the effect has expired.
     * @return True if expired, false otherwise
     */
    public boolean hasExpired() {
        if (getTimeLeft() == -1) {
            return false;
        }
        
        return getTimeLeft() <= 0;
    }
    
    /**
     * Activates this effect.
     */
    public void activate() {
        this.activated = true;
        this.stampActivated = TimeUtil.getUnixTimestamp();
    }
}
