package com.uber.server.game.rooms.services;

import com.uber.server.game.rooms.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing room bans.
 * Room bans are temporary (15 minute expiry) and stored in memory only.
 */
public class RoomBanService {
    private static final Logger logger = LoggerFactory.getLogger(RoomBanService.class);
    
    private final Room room;
    private final ConcurrentHashMap<Long, Long> bans; // User ID -> Ban timestamp
    
    public RoomBanService(Room room, ConcurrentHashMap<Long, Long> bans) {
        this.room = room;
        this.bans = bans;
    }
    
    /**
     * Checks if a user is banned from the room.
     */
    public boolean userIsBanned(long userId) {
        return bans.containsKey(userId);
    }
    
    /**
     * Checks if a user's ban has expired.
     * Room bans expire after 15 minutes (900 seconds).
     */
    public boolean hasBanExpired(long userId) {
        if (!userIsBanned(userId)) {
            return true;
        }
        
        long banTimestamp = bans.get(userId);
        long diff = com.uber.server.util.TimeUtil.getUnixTimestamp() - banTimestamp;
        
        // Bans expire after 900 seconds (15 minutes)
        return diff > 900;
    }
    
    /**
     * Removes a ban from a user.
     */
    public void removeBan(long userId) {
        bans.remove(userId);
    }
    
    /**
     * Adds a ban to a user.
     * Room bans are temporary (15 minute expiry) and stored in memory only.
     */
    public void addBan(long userId) {
        bans.put(userId, com.uber.server.util.TimeUtil.getUnixTimestamp());
    }
}
