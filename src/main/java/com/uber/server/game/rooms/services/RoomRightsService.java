package com.uber.server.game.rooms.services;

import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.rooms.Room;
import com.uber.server.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for managing room rights (room managers).
 * Handles loading, adding, and removing room rights.
 */
public class RoomRightsService {
    private static final Logger logger = LoggerFactory.getLogger(RoomRightsService.class);
    
    private final Room room;
    private final RoomRepository roomRepository;
    private final CopyOnWriteArrayList<Long> usersWithRights;
    
    public RoomRightsService(Room room, RoomRepository roomRepository, 
                      CopyOnWriteArrayList<Long> usersWithRights) {
        this.room = room;
        this.roomRepository = roomRepository;
        this.usersWithRights = usersWithRights;
    }
    
    /**
     * Loads room rights (room managers) from database.
     */
    public void loadRights() {
        usersWithRights.clear();
        
        List<Long> rights = roomRepository.loadRoomRights(room.getRoomId());
        usersWithRights.addAll(rights);
    }
    
    /**
     * Checks if a user has room rights.
     */
    public boolean checkRights(GameClient session) {
        return checkRights(session, false);
    }
    
    /**
     * Checks if a user has room rights.
     */
    public boolean checkRights(GameClient session, boolean requireOwnership) {
        if (session == null || session.getHabbo() == null) {
            return false;
        }
        
        Habbo habbo = session.getHabbo();
        
        // Owner always has rights
        if (habbo.getUsername().toLowerCase().equals(room.getData().getOwner().toLowerCase())) {
            return true;
        }
        
        // Check admin fuses
        if (habbo.hasFuse("fuse_admin") || habbo.hasFuse("fuse_any_room_controller")) {
            return true;
        }
        
        if (!requireOwnership) {
            // Check room rights fuse
            if (habbo.hasFuse("fuse_any_room_rights")) {
                return true;
            }
            
            // Check if user has room rights
            if (usersWithRights.contains(habbo.getId())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Adds a room right (room manager).
     */
    public boolean addRight(long userId) {
        if (usersWithRights.contains(userId)) {
            return false; // Already has rights
        }
        
        if (roomRepository.addRoomRight(room.getRoomId(), userId)) {
            usersWithRights.add(userId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes a room right.
     */
    public boolean removeRight(long userId) {
        if (!usersWithRights.contains(userId)) {
            return false;
        }
        
        if (roomRepository.deleteRoomRights(room.getRoomId(), new long[]{userId})) {
            usersWithRights.remove(userId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes all room rights.
     */
    public boolean removeAllRights() {
        if (usersWithRights.isEmpty()) {
            return true;
        }
        
        if (roomRepository.deleteRoomRights(room.getRoomId(), null)) {
            usersWithRights.clear();
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets list of users with rights.
     */
    public List<Long> getUsersWithRights() {
        return new java.util.ArrayList<>(usersWithRights);
    }
    
    /**
     * Checks if a user has rights.
     */
    public boolean hasRights(long userId) {
        return usersWithRights.contains(userId);
    }
}
