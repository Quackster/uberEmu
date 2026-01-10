package com.uber.server.repository;

import com.uber.server.storage.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Repository for moodlight database operations.
 */
public class MoodlightRepository {
    private static final Logger logger = LoggerFactory.getLogger(MoodlightRepository.class);
    private final DatabasePool databasePool;
    
    public MoodlightRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Loads moodlight data for an item.
     * @param itemId Item ID
     * @return Moodlight data (enabled, current_preset, preset_one, preset_two, preset_three), or null if not found
     */
    public MoodlightData loadMoodlight(long itemId) {
        String sql = """
            SELECT enabled, current_preset, preset_one, preset_two, preset_three
            FROM room_items_moodlight WHERE item_id = ? LIMIT 1
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    MoodlightData data = new MoodlightData();
                    data.enabled = rs.getString("enabled").equals("1");
                    data.currentPreset = rs.getInt("current_preset");
                    data.presetOne = rs.getString("preset_one");
                    data.presetTwo = rs.getString("preset_two");
                    data.presetThree = rs.getString("preset_three");
                    return data;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load moodlight data for item {}: {}", itemId, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Creates a new moodlight entry.
     * @param itemId Item ID
     * @return True if creation was successful
     */
    public boolean createMoodlight(long itemId) {
        String sql = """
            INSERT INTO room_items_moodlight (item_id, enabled, current_preset, preset_one, preset_two, preset_three)
            VALUES (?, '0', '1', '#000000,255,0', '#000000,255,0', '#000000,255,0')
            """;
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, itemId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to create moodlight for item {}: {}", itemId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates moodlight enabled state.
     * @param itemId Item ID
     * @param enabled Enabled state
     * @return True if update was successful
     */
    public boolean updateEnabled(long itemId, boolean enabled) {
        String sql = "UPDATE room_items_moodlight SET enabled = ? WHERE item_id = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, enabled ? "1" : "0");
            stmt.setLong(2, itemId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update moodlight enabled state for item {}: {}", itemId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates a moodlight preset.
     * @param itemId Item ID
     * @param preset Preset number (1, 2, or 3)
     * @param colorCode Color code (e.g., "#000000")
     * @param intensity Intensity (0-255)
     * @param backgroundOnly Background only flag
     * @return True if update was successful
     */
    public boolean updatePreset(long itemId, int preset, String colorCode, int intensity, boolean backgroundOnly) {
        String presetColumn = switch (preset) {
            case 3 -> "preset_three";
            case 2 -> "preset_two";
            default -> "preset_one";
        };
        
        String sql = "UPDATE room_items_moodlight SET " + presetColumn + " = ? WHERE item_id = ? LIMIT 1";
        String presetValue = colorCode + "," + intensity + "," + (backgroundOnly ? "1" : "0");
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, presetValue);
            stmt.setLong(2, itemId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update moodlight preset for item {}: {}", itemId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Data class for moodlight information.
     */
    public static class MoodlightData {
        public boolean enabled;
        public int currentPreset;
        public String presetOne;
        public String presetTwo;
        public String presetThree;
    }
}
