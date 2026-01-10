package com.uber.server.game.rooms.services;

import com.uber.server.game.GameClient;
import com.uber.server.game.rooms.RoomData;
import com.uber.server.game.rooms.RoomModel;
import com.uber.server.repository.RoomRepository;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Service for creating rooms.
 * Extracted from RoomManager.
 */
public class RoomCreationService {
    private static final Logger logger = LoggerFactory.getLogger(RoomCreationService.class);
    
    private final RoomRepository roomRepository;
    private final RoomLoadService roomLoadService;
    
    public RoomCreationService(RoomRepository roomRepository, RoomLoadService roomLoadService) {
        this.roomRepository = roomRepository;
        this.roomLoadService = roomLoadService;
    }
    
    /**
     * Creates a new room.
     * @param client GameClient creating the room
     * @param roomName Room name
     * @param modelName Model name
     * @param models Map of available room models
     * @return RoomData for the created room, or null if creation failed
     */
    public RoomData createRoom(GameClient client, String roomName, String modelName, 
                              Map<String, RoomModel> models) {
        if (client == null || client.getHabbo() == null) {
            return null;
        }
        
        // Filter injection characters
        roomName = StringUtil.filterInjectionChars(roomName, true);
        
        // Validate model exists
        if (models == null || !models.containsKey(modelName)) {
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
        return roomLoadService.generateRoomData(roomId);
    }
}
