package com.uber.server.game.rooms.services;

import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for managing room events.
 * Extracted from RoomManager.
 */
public class RoomEventService {
    private static final Logger logger = LoggerFactory.getLogger(RoomEventService.class);
    
    /**
     * Gets event rooms for a category.
     * @param rooms Map of room ID to Room instances
     * @param categoryId Category ID (0 for all categories)
     * @return List of rooms with ongoing events in the specified category
     */
    public List<Room> getEventRoomsForCategory(Map<Long, Room> rooms, int categoryId) {
        if (rooms == null) {
            return new ArrayList<>();
        }
        
        List<Room> eventRooms = new ArrayList<>();
        
        // Iterate through all loaded rooms and find those with events
        for (Room room : rooms.values()) {
            if (room != null && room.hasOngoingEvent()) {
                RoomEvent event = room.getEvent();
                if (event != null) {
                    // If categoryId is 0, return all events; otherwise filter by category
                    if (categoryId == 0 || event.getCategory() == categoryId) {
                        eventRooms.add(room);
                    }
                }
            }
        }
        
        return eventRooms;
    }
}
