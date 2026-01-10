package com.uber.server.game.support;

import com.uber.server.util.TimeUtil;

/**
 * Represents a moderation ban.
 */
public class ModerationBan {
    private final ModerationBanType type;
    private final String variable;
    private final String reasonMessage;
    private final long expire;
    
    public ModerationBan(ModerationBanType type, String variable, String reasonMessage, long expire) {
        this.type = type;
        this.variable = variable;
        this.reasonMessage = reasonMessage;
        this.expire = expire;
    }
    
    /**
     * Checks if the ban has expired.
     * @return True if ban has expired, false otherwise
     */
    public boolean isExpired() {
        return TimeUtil.getUnixTimestamp() >= expire;
    }
    
    // Getters
    public ModerationBanType getType() { return type; }
    public String getVariable() { return variable; }
    public String getReasonMessage() { return reasonMessage; }
    public long getExpire() { return expire; }
}
