package com.uber.server.game.rooms.services;

import com.uber.server.game.rooms.Room;
import com.uber.server.game.rooms.RoomModel;
import com.uber.server.game.rooms.RoomUser;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing bots and pets in a room.
 * Handles deploying, removing, and initializing bots/pets.
 */
public class RoomBotService {
    private static final Logger logger = LoggerFactory.getLogger(RoomBotService.class);
    
    private final Room room;
    private final ConcurrentHashMap<Long, RoomUser> users;
    private final RoomValidation validation;
    private int userCounter;
    
    public RoomBotService(Room room, ConcurrentHashMap<Long, RoomUser> users, 
                         RoomValidation validation, int initialUserCounter) {
        this.room = room;
        this.users = users;
        this.validation = validation;
        this.userCounter = initialUserCounter;
    }
    
    /**
     * Sets the user counter (used for virtual IDs).
     */
    public void setUserCounter(int counter) {
        this.userCounter = counter;
    }
    
    /**
     * Gets the current user counter.
     */
    public int getUserCounter() {
        return userCounter;
    }
    
    /**
     * Deploys a pet as a bot in the room (simplified version).
     */
    public RoomUser deployPet(com.uber.server.game.pets.Pet pet, int x, int y) {
        if (pet == null) {
            return null;
        }
        
        RoomModel model = room.getModel();
        if (model == null) {
            return null;
        }
        
        // Validate position
        if (x < 0 || y < 0 || x >= model.getMapSizeX() || y >= model.getMapSizeY()) {
            x = model.getDoorX();
            y = model.getDoorY();
        }
        
        if (!validation.canWalk(x, y, 0.0, true)) {
            return null;
        }
        
        // Calculate Z from model (simplified - full pathfinding would use SqAbsoluteHeight)
        double z = model.getDoorZ();
        
        // Create RoomUser for pet (habboId = 0 for bots/pets)
        RoomUser petUser = new RoomUser(0, room.getRoomId(), userCounter++, room.getGame());
        petUser.setPos(x, y, z);
        petUser.setRot(model.getDoorDir());
        petUser.setBot(true);
        petUser.setPet(true);
        petUser.setPetData(pet);
        
        pet.setRoomId(room.getRoomId());
        pet.setPlacedInRoom(true);
        pet.setX(x);
        pet.setY(y);
        pet.setZ(z);
        pet.setVirtualId(petUser.getVirtualId());
        
        // Use petId as key for pets (add large offset to avoid collisions with user IDs)
        long petKey = pet.getPetId() + 1000000000L; // Large offset to avoid collisions
        users.put(petKey, petUser);
        
        // Send pet entry message to room
        ServerMessage enterMessage = new ServerMessage(28);
        enterMessage.appendInt32(1);
        petUser.serialize(enterMessage);
        room.sendMessage(enterMessage);
        
        return petUser;
    }
    
    /**
     * Initializes bots for this room.
     */
    public void initBots() {
        if (room.getGame() == null || room.getGame().getBotManager() == null) {
            return;
        }
        
        List<com.uber.server.game.bots.RoomBot> bots = room.getGame().getBotManager().getBotsForRoom(room.getRoomId());
        for (com.uber.server.game.bots.RoomBot bot : bots) {
            deployBot(bot);
        }
    }
    
    /**
     * Initializes pets for this room.
     */
    public void initPets() {
        if (room.getGame() == null || room.getGame().getPetRepository() == null || room.getGame().getCatalog() == null) {
            return;
        }
        
        // Load pets from database for this room
        List<Map<String, Object>> petData = room.getGame().getPetRepository().loadPetsInRoom(room.getRoomId());
        for (Map<String, Object> row : petData) {
            try {
                // Use Catalog to generate pet from database row
                com.uber.server.game.pets.Pet pet = room.getGame().getCatalog().generatePetFromRow(row);
                if (pet == null) {
                    continue;
                }
                
                // Create a RoomBot for the pet
                Map<String, Object> botData = new HashMap<>();
                botData.put("id", pet.getPetId());
                botData.put("room_id", room.getRoomId());
                botData.put("ai_type", "pet");
                botData.put("walk_mode", "freeroam");
                botData.put("name", pet.getName() != null ? pet.getName() : "");
                botData.put("motto", "");
                botData.put("look", pet.getLook() != null ? pet.getLook() : "");
                botData.put("x", pet.getX());
                botData.put("y", pet.getY());
                botData.put("z", (int) pet.getZ());
                botData.put("rotation", 0);
                botData.put("min_x", 0);
                botData.put("min_y", 0);
                botData.put("max_x", 0);
                botData.put("max_y", 0);
                
                com.uber.server.game.bots.RoomBot petBot = new com.uber.server.game.bots.RoomBot(
                    botData,
                    null // BotRepository not needed for pets
                );
                
                deployBot(petBot, pet);
            } catch (Exception e) {
                logger.error("Failed to load pet for room {}: {}", room.getRoomId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * Deploys a bot to the room.
     */
    public RoomUser deployBot(com.uber.server.game.bots.RoomBot bot) {
        return deployBot(bot, null);
    }
    
    /**
     * Deploys a bot to the room (with optional pet data).
     */
    public RoomUser deployBot(com.uber.server.game.bots.RoomBot bot, com.uber.server.game.pets.Pet petData) {
        if (bot == null) {
            return null;
        }
        
        RoomModel model = room.getModel();
        if (model == null) {
            return null;
        }
        
        RoomUser botUser = new RoomUser(0, room.getRoomId(), userCounter++, room.getGame());
        
        // Set position
        int botX = bot.getX();
        int botY = bot.getY();
        int botZ = bot.getZ();
        
        if ((botX > 0 && botY > 0) && botX < model.getMapSizeX() && botY < model.getMapSizeY()) {
            botUser.setPos(botX, botY, botZ);
            botUser.setRot(bot.getRot());
        } else {
            // Use door position
            botX = model.getDoorX();
            botY = model.getDoorY();
            botZ = (int) model.getDoorZ();
            botUser.setPos(botX, botY, botZ);
            botUser.setRot(model.getDoorDir());
        }
        
        // Set bot properties
        botUser.setBot(true);
        botUser.setBotData(bot);
        
        if (petData != null) {
            botUser.setPet(true);
            botUser.setPetData(petData);
            petData.setVirtualId(botUser.getVirtualId());
        }
        
        // Generate BotAI
        com.uber.server.game.bots.BotAI botAI = bot.generateBotAI(botUser.getVirtualId());
        if (bot.isPet() && petData != null) {
            botAI.init((int) bot.getBotId(), botUser.getVirtualId(), room.getRoomId());
        } else {
            botAI.init(-1, botUser.getVirtualId(), room.getRoomId());
        }
        botUser.setBotAI(botAI);
        
        // Add to users map (use botId as key with offset for bots)
        long botKey = bot.getBotId() + 2000000000L; // Different offset than pets
        users.put(botKey, botUser);
        
        // Update status
        botUser.setUpdateNeeded(true);
        
        // Send entry message
        ServerMessage enterMessage = new ServerMessage(28);
        enterMessage.appendInt32(1);
        botUser.serialize(enterMessage);
        room.sendMessage(enterMessage);
        
        // Call bot AI on enter
        botAI.onSelfEnterRoom();
        
        return botUser;
    }
    
    /**
     * Removes a bot/pet from room.
     */
    public void removeBot(int virtualId, boolean kicked) {
        RoomUser botUser = room.getRoomUserByVirtualId(virtualId);
        if (botUser == null || !botUser.isBot()) {
            return;
        }
        
        // Call bot AI on leave
        if (botUser.getBotAI() != null) {
            botUser.getBotAI().onSelfLeaveRoom(kicked);
        }
        
        // Send leave message
        ServerMessage leaveMessage = new ServerMessage(29);
        leaveMessage.appendInt32(virtualId);
        room.sendMessage(leaveMessage);
        
        // Remove from users - find by virtualId since we need to find the right key
        long keyToRemove = -1;
        for (Map.Entry<Long, RoomUser> entry : users.entrySet()) {
            if (entry.getValue().getVirtualId() == virtualId) {
                keyToRemove = entry.getKey();
                break;
            }
        }
        if (keyToRemove >= 0) {
            users.remove(keyToRemove);
        }
        
        // If it's a pet, update pet data and database
        if (botUser.isPet() && botUser.getPetData() != null) {
            com.uber.server.game.pets.Pet pet = botUser.getPetData();
            pet.setPlacedInRoom(false);
            pet.setRoomId(0);
            pet.setX(0);
            pet.setY(0);
            pet.setZ(0.0);
            
            // Update database
            if (room.getGame() != null && room.getGame().getPetRepository() != null) {
                room.getGame().getPetRepository().updatePetRoom(pet.getPetId(), 0, 0, 0, 0.0);
            }
        }
    }
}
