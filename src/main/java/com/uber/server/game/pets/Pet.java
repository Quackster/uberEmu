package com.uber.server.game.pets;

import com.uber.server.game.Game;
import com.uber.server.game.GameClientManager;
import com.uber.server.messages.ServerMessage;
import com.uber.server.repository.PetRepository;
import com.uber.server.game.rooms.Room;
import com.uber.server.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Represents a pet.
 */
public class Pet {
    private static final Logger logger = LoggerFactory.getLogger(Pet.class);
    
    private long petId;
    private long ownerId;
    private int virtualId;
    
    private int type;
    private String name;
    private String race;
    private String color;
    
    private int experience;
    private int energy;
    private int nutrition;
    
    private long roomId;
    private int x;
    private int y;
    private double z;
    
    private int respect;
    
    private double creationStamp;
    private boolean placedInRoom;
    
    private final PetRepository repository;
    private final Game game;
    
    public Pet(long petId, long ownerId, long roomId, String name, int type, String race, String color,
               int experience, int energy, int nutrition, int respect, double creationStamp,
               int x, int y, double z, PetRepository repository, Game game) {
        this.petId = petId;
        this.ownerId = ownerId;
        this.roomId = roomId;
        this.name = name;
        this.type = type;
        this.race = race;
        this.color = color;
        this.experience = experience;
        this.energy = energy;
        this.nutrition = nutrition;
        this.respect = respect;
        this.creationStamp = creationStamp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.placedInRoom = false;
        this.virtualId = 0;
        this.repository = repository;
        this.game = game;
    }
    
    /**
     * Creates a Pet from a database row map.
     * @param row Database row map
     * @param repository PetRepository instance
     * @param game Game instance
     * @return Pet instance
     */
    public static Pet fromRow(Map<String, Object> row, PetRepository repository, Game game) {
        long petId = ((Number) row.get("id")).longValue();
        long ownerId = ((Number) row.get("user_id")).longValue();
        long roomId = ((Number) row.get("room_id")).longValue();
        String name = (String) row.get("name");
        int type = ((Number) row.get("type")).intValue();
        String race = (String) row.get("race");
        String color = (String) row.get("color");
        int experience = ((Number) row.get("expirience")).intValue();
        int energy = ((Number) row.get("energy")).intValue();
        int nutrition = ((Number) row.get("nutrition")).intValue();
        int respect = ((Number) row.get("respect")).intValue();
        double creationStamp = ((Number) row.get("createstamp")).doubleValue();
        int x = row.get("x") != null ? ((Number) row.get("x")).intValue() : 0;
        int y = row.get("y") != null ? ((Number) row.get("y")).intValue() : 0;
        double z = row.get("z") != null ? ((Number) row.get("z")).doubleValue() : 0.0;
        
        return new Pet(petId, ownerId, roomId, name, type, race, color, experience, energy, 
                      nutrition, respect, creationStamp, x, y, z, repository, game);
    }
    
    /**
     * Gets the room this pet is in.
     * @return Room, or null if not in a room
     */
    public Room getRoom() {
        if (!isInRoom()) {
            return null;
        }
        
        if (game != null && game.getRoomManager() != null) {
            return game.getRoomManager().getRoom(roomId);
        }
        
        return null;
    }
    
    /**
     * Checks if pet is in a room.
     * @return True if in room
     */
    public boolean isInRoom() {
        return roomId > 0;
    }
    
    /**
     * Gets pet level (always 1 for now).
     * @return Pet level
     */
    public int getLevel() {
        return 1; // Simplified - can calculate based on experience later
    }
    
    /**
     * Gets max level.
     * @return Max level (20)
     */
    public int getMaxLevel() {
        return 20;
    }
    
    /**
     * Gets experience goal for current level.
     * @return Experience goal (100)
     */
    public int getExperienceGoal() {
        return 100; // Simplified
    }
    
    /**
     * Gets max energy.
     * @return Max energy (100)
     */
    public int getMaxEnergy() {
        return 100;
    }
    
    /**
     * Gets max nutrition.
     * @return Max nutrition (150)
     */
    public int getMaxNutrition() {
        return 150;
    }
    
    /**
     * Gets pet age in days.
     * @return Age in days
     */
    public int getAge() {
        long currentTimestamp = TimeUtil.getUnixTimestamp();
        return (int) Math.floor((currentTimestamp - creationStamp) / 86400.0);
    }
    
    /**
     * Gets pet look string.
     * @return Look string (type race color)
     */
    public String getLook() {
        return type + " " + race + " " + color;
    }
    
    /**
     * Gets owner name.
     * @return Owner name, or empty string if not found
     */
    public String getOwnerName() {
        if (game != null && game.getClientManager() != null) {
            GameClientManager clientManager = game.getClientManager();
            String name = clientManager.getNameById(ownerId);
            return name != null ? name : "";
        }
        return "";
    }
    
    /**
     * Handles respect given to pet.
     */
    public void onRespect() {
        respect++;
        
        if (repository != null) {
            repository.incrementRespect(petId);
        }
        
        addExperience(10);
    }
    
    /**
     * Adds experience to pet.
     * @param amount Experience amount to add
     */
    public void addExperience(int amount) {
        experience += amount;
        
        if (repository != null) {
            repository.updateExperience(petId, experience);
        }
        
        Room room = getRoom();
        if (room != null) {
            ServerMessage message = new ServerMessage(609);
            message.appendUInt(petId);
            message.appendInt32(virtualId);
            message.appendInt32(amount);
            room.sendMessage(message);
        }
        
        // Level up logic could go here when experience > experienceGoal
    }
    
    /**
     * Serializes pet for inventory display.
     * @param message ServerMessage to append to
     */
    public void serializeInventory(ServerMessage message) {
        message.appendUInt(petId);
        message.appendStringWithBreak(name != null ? name : "");
        message.appendStringWithBreak(getLook());
        message.appendBoolean(false); // Unknown flag
    }
    
    /**
     * Serializes pet info.
     * @return ServerMessage with pet info (ID 601)
     */
    public ServerMessage serializeInfo() {
        ServerMessage info = new ServerMessage(601);
        info.appendUInt(petId);
        info.appendStringWithBreak(name != null ? name : "");
        info.appendInt32(getLevel());
        info.appendInt32(getMaxLevel());
        info.appendInt32(experience);
        info.appendInt32(getExperienceGoal());
        info.appendInt32(energy);
        info.appendInt32(getMaxEnergy());
        info.appendInt32(nutrition);
        info.appendInt32(getMaxNutrition());
        info.appendStringWithBreak(getLook());
        info.appendInt32(respect);
        info.appendUInt(ownerId);
        info.appendInt32(getAge());
        info.appendStringWithBreak(getOwnerName());
        return info;
    }
    
    // Getters and setters
    public long getPetId() { return petId; }
    public long getOwnerId() { return ownerId; }
    public int getVirtualId() { return virtualId; }
    public void setVirtualId(int virtualId) { this.virtualId = virtualId; }
    public int getType() { return type; }
    public String getName() { return name; }
    public String getRace() { return race; }
    public String getColor() { return color; }
    public int getExperience() { return experience; }
    public int getEnergy() { return energy; }
    public int getNutrition() { return nutrition; }
    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public int getRespect() { return respect; }
    public double getCreationStamp() { return creationStamp; }
    public boolean isPlacedInRoom() { return placedInRoom; }
    public void setPlacedInRoom(boolean placedInRoom) { this.placedInRoom = placedInRoom; }
    public void setExperience(int experience) { this.experience = experience; }
    public void setRespect(int respect) { this.respect = respect; }
}
