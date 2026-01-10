package com.uber.server.game.rooms.services;

import com.uber.server.game.rooms.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing room bans.
 * Extracted from Room class to improve separation of concerns.
 * Room bans are temporary (15 minutes) and in-memory only.
 */
public class RoomBanService {
    private static final Logger logger = LoggerFactory.getLogger(RoomBanService.class);
    private static final long BAN_DURATION_MS = 15 * 60 * 1000; // 15 minutes
    
    private final Room room;
    private final ConcurrentHashMap<Long, Long> bans; // User ID -> Ban timestamp
    
    public RoomBanService(Room room) {
        this.room = room;
        this.bans = new ConcurrentHashMap<>();
    }
    
    /**
     * Checks if a user is banned from the room.
     * @param userId User ID
     * @return True if user is banned and ban hasn't expired
     */
    public boolean userIsBanned(long userId) {
        if (!bans.containsKey(userId)) {
            return false;
        }
        
        // Check if ban has expired
        if (hasBanExpired(userId)) {
            removeBan(userId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if a ban has expired.
     * @param userId User ID
     * @return True if ban has expired
     */
    public boolean hasBanExpired(long userId) {
        Long banTimestamp = bans.get(userId);
        if (banTimestamp == null) {
            return true;
        }
        
        long now = System.currentTimeMillis();
        return (now - banTimestamp) > BAN_DURATION_MS;
    }
    
    /**
     * Removes a ban for a user.
     * @param userId User ID
     */
    public void removeBan(long userId) {
        bans.remove(userId);
    }
    
    /**
     * Adds a ban for a user.
     * @param userId User ID
     */
    public void addBan(long userId) {
        bans.put(userId, System.currentTimeMillis());
    }
    
    /**
     * Clears all bans.
     */
    public void clear() {
        bans.clear();
    }
}
