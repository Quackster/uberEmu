package com.uber.server.game.rooms.services;

import com.uber.server.game.GameClient;
import com.uber.server.game.items.RoomItem;
import com.uber.server.game.rooms.Room;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.RoomItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing items in a room.
 * Handles loading, placing, and removing room items.
 */
public class RoomItemService {
    private static final Logger logger = LoggerFactory.getLogger(RoomItemService.class);
    
    private final Room room;
    private final ConcurrentHashMap<Long, RoomItem> items;
    private final RoomItemRepository roomItemRepository;
    private final com.uber.server.game.items.MoodlightData[] moodlightDataRef; // Use array to allow modification
    
    public RoomItemService(Room room, ConcurrentHashMap<Long, RoomItem> items, 
                          RoomItemRepository roomItemRepository,
                          com.uber.server.game.items.MoodlightData[] moodlightDataRef) {
        this.room = room;
        this.items = items;
        this.roomItemRepository = roomItemRepository;
        this.moodlightDataRef = moodlightDataRef;
    }
    
    /**
     * Loads room items from database.
     */
    public void loadItems() {
        items.clear();
        
        List<Map<String, Object>> itemData = roomItemRepository.loadRoomItems(room.getRoomId());
        for (Map<String, Object> row : itemData) {
            try {
                long id = ((Number) row.get("id")).longValue();
                long baseItem = ((Number) row.get("base_item")).longValue();
                String extraData = (String) row.get("extra_data");
                int x = ((Number) row.get("x")).intValue();
                int y = ((Number) row.get("y")).intValue();
                double z = ((Number) row.get("z")).doubleValue();
                int rot = ((Number) row.get("rot")).intValue();
                String wallPos = (String) row.get("wall_pos");
                
                RoomItem item = new RoomItem(id, room.getRoomId(), baseItem, extraData, x, y, z, rot, wallPos, room.getGame());
                items.put(id, item);
                
                // Initialize moodlight data if this is a dimmer item
                com.uber.server.game.items.Item baseItemObj = room.getGame().getItemManager().getItem(baseItem);
                if (baseItemObj != null && "dimmer".equalsIgnoreCase(baseItemObj.getInteractionType())) {
                    try {
                        moodlightDataRef[0] = new com.uber.server.game.items.MoodlightData(id, roomItemRepository);
                        // Update Room's moodlightData field
                        room.setMoodlightData(moodlightDataRef[0]);
                    } catch (Exception e) {
                        logger.warn("Failed to load moodlight data for item {}: {}", id, e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to load room item: {}", e.getMessage(), e);
            }
        }
        
        logger.debug("Loaded {} items for room {}", items.size(), room.getRoomId());
    }
    
    /**
     * Gets a room item by ID.
     */
    public RoomItem getItem(long itemId) {
        return items.get(itemId);
    }
    
    /**
     * Gets list of floor items.
     */
    public List<RoomItem> getFloorItems() {
        List<RoomItem> floorItems = new ArrayList<>();
        for (RoomItem item : items.values()) {
            if (item.isFloorItem()) {
                floorItems.add(item);
            }
        }
        return floorItems;
    }
    
    /**
     * Gets list of wall items.
     */
    public List<RoomItem> getWallItems() {
        List<RoomItem> wallItems = new ArrayList<>();
        for (RoomItem item : items.values()) {
            if (item.isWallItem()) {
                wallItems.add(item);
            }
        }
        return wallItems;
    }
    
    /**
     * Places a floor item in the room (simplified version).
     */
    public boolean setFloorItem(GameClient session, RoomItem item, int newX, int newY, int newRot, boolean newItem) {
        if (item == null || session == null) {
            return false;
        }
        
        com.uber.server.game.items.Item baseItem = item.getBaseItem();
        if (baseItem == null || !baseItem.isFloorItem()) {
            return false;
        }
        
        // Validate rotation
        if (newRot != 0 && newRot != 2 && newRot != 4 && newRot != 6 && newRot != 8) {
            newRot = 0;
        }
        
        // Get room model for basic validation
        com.uber.server.game.rooms.RoomModel model = room.getModel();
        if (model == null) {
            return false;
        }
        
        // Calculate Z coordinate (simplified - use floor height from model if available)
        double newZ = 0.0; // Default to 0, will be enhanced with model heightmap
        
        // Update item position
        item.setX(newX);
        item.setY(newY);
        item.setZ(newZ);
        item.setRot(newRot);
        
        // Call item interactor OnPlace
        item.getInteractor().onPlace(session, item);
        
        if (newItem) {
            // Insert into database
            if (roomItemRepository.createRoomItem(item.getId(), room.getRoomId(), item.getBaseItemId(), 
                                                  item.getExtraData(), newX, newY, newZ, newRot, "")) {
                items.put(item.getId(), item);
                
                // Send item to room
                ServerMessage message = new ServerMessage(93);
                item.serialize(message);
                room.sendMessage(message);
                return true;
            }
        } else {
            // Update in database
            if (roomItemRepository.updatePosition(item.getId(), newX, newY, newZ, newRot)) {
                // Send update to room
                ServerMessage message = new ServerMessage(95);
                item.serialize(message);
                room.sendMessage(message);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Places a wall item in the room (simplified version).
     */
    public boolean setWallItem(GameClient session, RoomItem item) {
        if (item == null || session == null) {
            return false;
        }
        
        com.uber.server.game.items.Item baseItem = item.getBaseItem();
        if (baseItem == null || !baseItem.isWallItem()) {
            return false;
        }
        
        // Call item interactor OnPlace
        item.getInteractor().onPlace(session, item);
        
        // Handle special item types
        String interactionType = baseItem.getInteractionType();
        if ("dimmer".equalsIgnoreCase(interactionType)) {
            // Initialize MoodlightData for dimmer items
            try {
                moodlightDataRef[0] = new com.uber.server.game.items.MoodlightData(item.getId(), roomItemRepository);
                // Update Room's moodlightData field
                room.setMoodlightData(moodlightDataRef[0]);
            } catch (Exception e) {
                logger.warn("Failed to initialize moodlight data for item {}: {}", item.getId(), e.getMessage());
            }
        }
        
        // Insert into database
        if (roomItemRepository.createRoomItem(item.getId(), room.getRoomId(), item.getBaseItemId(),
                                              item.getExtraData(), 0, 0, 0.0, 0, item.getWallPos())) {
            items.put(item.getId(), item);
            
            // Send item to room
            ServerMessage message = new ServerMessage(83);
            item.serialize(message);
            room.sendMessage(message);
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes furniture from the room.
     */
    public void removeFurniture(GameClient session, long itemId) {
        RoomItem item = getItem(itemId);
        if (item == null) {
            return;
        }
        
        // Call item interactor OnRemove
        item.getInteractor().onRemove(session, item);
        
        // Send removal message to room
        if (item.isWallItem()) {
            ServerMessage message = new ServerMessage(84);
            message.appendUInt(itemId);
            message.appendStringWithBreak("");
            message.appendBoolean(false);
            room.sendMessage(message);
        } else if (item.isFloorItem()) {
            ServerMessage message = new ServerMessage(94);
            message.appendUInt(itemId);
            message.appendStringWithBreak("");
            message.appendBoolean(false);
            room.sendMessage(message);
        }
        
        // Remove from room and database
        items.remove(itemId);
        roomItemRepository.deleteRoomItem(itemId);
    }
    
    /**
     * Counts items by interaction type.
     */
    public int itemCountByType(String interactionType) {
        if (interactionType == null) {
            return 0;
        }
        
        String lowerType = interactionType.toLowerCase();
        int count = 0;
        
        for (RoomItem item : items.values()) {
            com.uber.server.game.items.Item baseItem = item.getBaseItem();
            if (baseItem != null && lowerType.equals(baseItem.getInteractionType())) {
                count++;
            }
        }
        
        return count;
    }
}
