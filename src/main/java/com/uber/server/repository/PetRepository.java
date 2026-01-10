package com.uber.server.repository;

import com.uber.server.storage.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for pet database operations.
 */
public class PetRepository {
    private static final Logger logger = LoggerFactory.getLogger(PetRepository.class);
    private final DatabasePool databasePool;
    
    public PetRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Increments respect count for a pet.
     * @param petId Pet ID
     * @return True if update was successful
     */
    public boolean incrementRespect(long petId) {
        String sql = "UPDATE user_pets SET respect = respect + 1 WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, petId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to increment respect for pet {}: {}", petId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates pet experience.
     * @param petId Pet ID
     * @param experience New experience value
     * @return True if update was successful
     */
    public boolean updateExperience(long petId, int experience) {
        String sql = "UPDATE user_pets SET expirience = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, experience);
            stmt.setLong(2, petId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update experience for pet {}: {}", petId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Creates a new pet.
     * @param userId User ID (owner)
     * @param name Pet name
     * @param type Pet type
     * @param race Pet race
     * @param color Pet color
     * @param createTimestamp Creation timestamp
     * @return The ID of the created pet, or 0 if failed
     */
    public long createPet(long userId, String name, int type, String race, String color, long createTimestamp) {
        String sql = """
            INSERT INTO user_pets (user_id, name, type, race, color, expirience, energy, createstamp)
            VALUES (?, ?, ?, ?, ?, 0, 100, ?)
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, userId);
            stmt.setString(2, name);
            stmt.setInt(3, type);
            stmt.setString(4, race);
            stmt.setString(5, color);
            stmt.setLong(6, createTimestamp);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to create pet: {}", e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Gets pet data by ID.
     * @param petId Pet ID
     * @return Pet data map, or null if not found
     */
    public Map<String, Object> getPet(long petId) {
        String sql = "SELECT * FROM user_pets WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, petId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> pet = new HashMap<>();
                    pet.put("id", rs.getLong("id"));
                    pet.put("user_id", rs.getLong("user_id"));
                    pet.put("name", rs.getString("name"));
                    pet.put("type", rs.getInt("type"));
                    pet.put("race", rs.getString("race"));
                    pet.put("color", rs.getString("color"));
                    pet.put("expirience", rs.getInt("expirience"));
                    pet.put("energy", rs.getInt("energy"));
                    pet.put("nutrition", rs.getInt("nutrition"));
                    pet.put("respect", rs.getInt("respect"));
                    pet.put("createstamp", rs.getDouble("createstamp"));
                    pet.put("room_id", rs.getLong("room_id"));
                    pet.put("x", rs.getInt("x"));
                    pet.put("y", rs.getInt("y"));
                    pet.put("z", rs.getDouble("z"));
                    return pet;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get pet {}: {}", petId, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Updates a pet's room position.
     * @param petId Pet ID
     * @param roomId Room ID (0 for inventory)
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return True if update was successful
     */
    public boolean updatePetRoom(long petId, long roomId, int x, int y, double z) {
        String sql = "UPDATE user_pets SET room_id = ?, x = ?, y = ?, z = ? WHERE id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            stmt.setInt(2, x);
            stmt.setInt(3, y);
            stmt.setDouble(4, z);
            stmt.setLong(5, petId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update pet {} room: {}", petId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Deletes all pets for a user.
     * @param userId User ID
     * @return True if deletion was successful
     */
    public boolean deleteAllPets(long userId) {
        String sql = "DELETE FROM user_pets WHERE user_id = ?";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Failed to delete all pets for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Loads all pets in a room.
     * @param roomId Room ID
     * @return List of pet data maps
     */
    public List<Map<String, Object>> loadPetsInRoom(long roomId) {
        String sql = "SELECT * FROM user_pets WHERE room_id = ?";
        List<Map<String, Object>> pets = new ArrayList<>();
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, roomId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> pet = new HashMap<>();
                    pet.put("id", rs.getLong("id"));
                    pet.put("user_id", rs.getLong("user_id"));
                    pet.put("name", rs.getString("name"));
                    pet.put("type", rs.getInt("type"));
                    pet.put("race", rs.getString("race"));
                    pet.put("color", rs.getString("color"));
                    pet.put("expirience", rs.getInt("expirience"));
                    pet.put("energy", rs.getInt("energy"));
                    pet.put("nutrition", rs.getInt("nutrition"));
                    pet.put("respect", rs.getInt("respect"));
                    pet.put("createstamp", rs.getDouble("createstamp"));
                    pet.put("room_id", rs.getLong("room_id"));
                    pet.put("x", rs.getInt("x"));
                    pet.put("y", rs.getInt("y"));
                    pet.put("z", rs.getDouble("z"));
                    pets.add(pet);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load pets in room {}: {}", roomId, e.getMessage(), e);
        }
        
        return pets;
    }
}
