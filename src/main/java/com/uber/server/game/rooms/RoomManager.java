package com.uber.server.game.rooms;

import com.uber.server.game.Game;
import com.uber.server.repository.RoomItemRepository;
import com.uber.server.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages room lifecycle and loading.
 */
public class RoomManager {
    private static final Logger logger = LoggerFactory.getLogger(RoomManager.class);
    
    private final ConcurrentHashMap<Long, Room> rooms;
    private final ConcurrentHashMap<String, RoomModel> models;
    private final RoomRepository roomRepository;
    private final RoomItemRepository roomItemRepository;
    private final Game game;
    
    public static final int MAX_PETS_PER_ROOM = 15;
    
    public RoomManager(RoomRepository roomRepository, RoomItemRepository roomItemRepository, Game game) {
        this.rooms = new ConcurrentHashMap<>();
        this.models = new ConcurrentHashMap<>();
        this.roomRepository = roomRepository;
        this.roomItemRepository = roomItemRepository;
        this.game = game;
    }
    
    /**
     * Loads room models from database.
     */
    public void loadModels() {
        models.clear();
        
        List<Map<String, Object>> modelData = roomRepository.loadRoomModels();
        for (Map<String, Object> row : modelData) {
            try {
                String id = (String) row.get("id");
                int doorX = ((Number) row.get("door_x")).intValue();
                int doorY = ((Number) row.get("door_y")).intValue();
                double doorZ = ((Number) row.get("door_z")).doubleValue();
                int doorDir = ((Number) row.get("door_dir")).intValue();
                String heightmap = (String) row.get("heightmap");
                String publicItems = (String) row.get("public_items");
                boolean clubOnly = "1".equals(row.get("club_only"));
                
                RoomModel model = new RoomModel(id, doorX, doorY, doorZ, doorDir, heightmap, publicItems, clubOnly);
                models.put(id, model);
            } catch (Exception e) {
                logger.error("Failed to load room model: {}", e.getMessage(), e);
            }
        }
        
        logger.info("Loaded {} room model(s).", models.size());
    }
    
    /**
     * Generates room data for a room ID.
     * @param roomId Room ID
     * @return RoomData, or null if not found
     */
    public RoomData generateRoomData(long roomId) {
        Map<String, Object> roomData = roomRepository.getRoomData(roomId);
        if (roomData == null) {
            return null;
        }
        
        RoomData data = new RoomData();
        data.fill(roomData);
        return data;
    }
    
    /**
     * Generates nullable room data (returns null data if room doesn't exist).
     */
    public RoomData generateNullableRoomData(long roomId) {
        RoomData data = generateRoomData(roomId);
        if (data == null) {
            data = new RoomData();
            data.fillNull(roomId);
        }
        return data;
    }
    
    /**
     * Checks if a room is loaded.
     */
    public boolean isRoomLoaded(long roomId) {
        return rooms.containsKey(roomId);
    }
    
    /**
     * Gets a room model.
     */
    public RoomModel getModel(String modelName) {
        if (modelName == null) {
            return null;
        }
        return models.get(modelName);
    }
    
    /**
     * Gets or loads a room.
     * @param roomId Room ID
     * @return Room instance, or null if room doesn't exist
     */
    public Room getRoom(long roomId) {
        // Check if already loaded
        Room room = rooms.get(roomId);
        if (room != null) {
            return room;
        }
        
        // Load room
        loadRoom(roomId);
        return rooms.get(roomId);
    }
    
    /**
     * Creates a new room.
     * @param client GameClient creating the room
     * @param roomName Room name
     * @param modelName Model name
     * @return RoomData for the created room, or null if creation failed
     */
    public RoomData createRoom(com.uber.server.game.GameClient client, String roomName, String modelName) {
        if (client == null || client.getHabbo() == null) {
            return null;
        }
        
        // Filter injection characters
        roomName = com.uber.server.util.StringUtil.filterInjectionChars(roomName, true);
        
        // Validate model exists
        if (!models.containsKey(modelName)) {
            client.sendNotif("Sorry, this room model has not been added yet. Try again later.");
            return null;
        }
        
        // Check if model is club-only
        RoomModel model = models.get(modelName);
        if (model.isClubOnly() && !client.getHabbo().hasFuse("fuse_use_special_room_layouts")) {
            client.sendNotif("You must be an Uber Club member to use that room layout.");
            return null;
        }
        
        // Validate name length
        if (roomName == null || roomName.length() < 3) {
            client.sendNotif("Room name is too short for room creation!");
            return null;
        }
        
        // Create room in database
        long roomId = roomRepository.createRoom(roomName, client.getHabbo().getUsername(), modelName);
        if (roomId == 0) {
            logger.error("Failed to create room for user {}", client.getHabbo().getId());
            return null;
        }
        
        // Generate and return room data
        return generateRoomData(roomId);
    }
    
    /**
     * Loads a room into memory.
     * @param roomId Room ID
     */
    public void loadRoom(long roomId) {
        if (rooms.containsKey(roomId)) {
            return;
        }
        
        RoomData data = generateRoomData(roomId);
        if (data == null) {
            logger.warn("Cannot load room {}: room data not found", roomId);
            return;
        }
        
        Room room = new Room(roomId, data, game, roomRepository, roomItemRepository);
        rooms.put(roomId, room);
        
        // Initialize bots and pets
        room.initBots();
        room.initPets();
        
        // Start process routine
        room.startProcessRoutine();
        
        logger.info("Loaded room: \"{}\" (ID: {})", data.getName(), roomId);
    }
    
    /**
     * Unloads a room from memory.
     * @param roomId Room ID
     */
    public void unloadRoom(long roomId) {
        Room room = rooms.remove(roomId);
        if (room != null) {
            room.destroy();
            logger.info("Unloaded room: \"{}\" (ID: {})", room.getData().getName(), roomId);
        }
    }
    
    /**
     * Requests room unload (marks for unloading).
     * @param roomId Room ID
     */
    public void requestRoomUnload(long roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return;
        }
        
        // Set keepAlive = false and stop process routine
        room.setKeepAlive(false);
        room.stopProcessRoutine();
        
        // Only unload if room has no users
        if (room.getUserCount() == 0) {
            unloadRoom(roomId);
        }
    }
    
    // Getters
    public Map<Long, Room> getRooms() {
        return new ConcurrentHashMap<>(rooms);
    }
    
    public int getLoadedRoomsCount() {
        return rooms.size();
    }
    
    /**
     * Gets event rooms for a category.
     * @param categoryId Category ID (0 for all categories)
     * @return List of rooms with ongoing events in the specified category
     */
    public List<Room> getEventRoomsForCategory(int categoryId) {
        List<Room> eventRooms = new java.util.ArrayList<>();
        
        // Iterate through all loaded rooms and find those with events
        for (Room room : rooms.values()) {
            if (room != null && room.hasOngoingEvent()) {
                com.uber.server.game.rooms.RoomEvent event = room.getEvent();
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
    
    public Map<String, RoomModel> getModels() {
        return new ConcurrentHashMap<>(models);
    }
}
