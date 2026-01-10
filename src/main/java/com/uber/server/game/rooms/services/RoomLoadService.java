package com.uber.server.game.rooms.services;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.repository.RoomItemRepository;
import com.uber.server.repository.RoomRepository;
import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomData;
import com.uber.server.game.rooms.RoomModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for loading room models and rooms.
 * Extracted from RoomManager.
 */
public class RoomLoadService {
    private static final Logger logger = LoggerFactory.getLogger(RoomLoadService.class);
    
    private final RoomRepository roomRepository;
    private final RoomItemRepository roomItemRepository;
    private final Game game;
    
    public RoomLoadService(RoomRepository roomRepository, 
                          RoomItemRepository roomItemRepository,
                          Game game) {
        this.roomRepository = roomRepository;
        this.roomItemRepository = roomItemRepository;
        this.game = game;
    }
    
    /**
     * Loads room models from database.
     * @param models Map to store loaded models
     */
    public void loadModels(ConcurrentHashMap<String, RoomModel> models) {
        if (models == null) {
            return;
        }
        
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
     * Loads a room into memory.
     * @param rooms Map to store loaded rooms
     * @param roomId Room ID
     */
    public void loadRoom(ConcurrentHashMap<Long, Room> rooms, long roomId) {
        if (rooms == null) {
            return;
        }
        
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
     * @param rooms Map of loaded rooms
     * @param roomId Room ID
     */
    public void unloadRoom(ConcurrentHashMap<Long, Room> rooms, long roomId) {
        if (rooms == null) {
            return;
        }
        
        Room room = rooms.remove(roomId);
        if (room != null) {
            room.destroy();
            logger.info("Unloaded room: \"{}\" (ID: {})", room.getData().getName(), roomId);
        }
    }
}
