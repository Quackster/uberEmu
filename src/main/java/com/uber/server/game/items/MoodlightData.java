package com.uber.server.game.items;

import com.uber.server.repository.RoomItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents moodlight data for a room.
 */
public class MoodlightData {
    private static final Logger logger = LoggerFactory.getLogger(MoodlightData.class);
    
    private boolean enabled;
    private int currentPreset;
    private final List<MoodlightPreset> presets;
    private final long itemId;
    private final RoomItemRepository repository;
    
    public MoodlightData(long itemId, RoomItemRepository repository) {
        this.itemId = itemId;
        this.repository = repository;
        this.presets = new CopyOnWriteArrayList<>();
        
        // Load from database
        Map<String, Object> row = repository.loadMoodlightData(itemId);
        if (row == null) {
            // Create default moodlight entry if it doesn't exist
            com.uber.server.repository.MoodlightRepository moodlightRepository = 
                com.uber.server.game.Game.getInstance().getMoodlightRepository();
            if (moodlightRepository != null) {
                moodlightRepository.createMoodlight(itemId);
                row = repository.loadMoodlightData(itemId);
            }
            
            if (row == null) {
                // Still null, use defaults
                this.enabled = false;
                this.currentPreset = 1;
                presets.add(generatePreset("#000000,255,0"));
                presets.add(generatePreset("#000000,255,0"));
                presets.add(generatePreset("#000000,255,0"));
                return;
            }
        }
        
        this.enabled = "1".equals(row.get("enabled"));
        this.currentPreset = ((Number) row.get("current_preset")).intValue();
        
        // Load presets
        String presetOne = (String) row.get("preset_one");
        String presetTwo = (String) row.get("preset_two");
        String presetThree = (String) row.get("preset_three");
        
        presets.add(generatePreset(presetOne != null ? presetOne : "#000000,255,0"));
        presets.add(generatePreset(presetTwo != null ? presetTwo : "#000000,255,0"));
        presets.add(generatePreset(presetThree != null ? presetThree : "#000000,255,0"));
    }
    
    /**
     * Enables the moodlight.
     */
    public void enable() {
        this.enabled = true;
        repository.updateMoodlightEnabled(itemId, true);
    }
    
    /**
     * Disables the moodlight.
     */
    public void disable() {
        this.enabled = false;
        repository.updateMoodlightEnabled(itemId, false);
    }
    
    /**
     * Updates a preset.
     * @param preset Preset number (1, 2, or 3)
     * @param colorCode Color code (e.g., "#EA4532")
     * @param intensity Intensity (0-255)
     * @param bgOnly Background only flag
     */
    public void updatePreset(int preset, String colorCode, int intensity, boolean bgOnly) {
        if (!isValidColor(colorCode) || !isValidIntensity(intensity)) {
            return;
        }
        
        String presetField = switch (preset) {
            case 3 -> "preset_three";
            case 2 -> "preset_two";
            default -> "preset_one";
        };
        
        String presetValue = colorCode + "," + intensity + "," + (bgOnly ? "1" : "0");
        repository.updateMoodlightPreset(itemId, presetField, presetValue);
        
        MoodlightPreset presetObj = getPreset(preset);
        presetObj.setColorCode(colorCode);
        presetObj.setColorIntensity(intensity);
        presetObj.setBackgroundOnly(bgOnly);
    }
    
    /**
     * Generates a preset from a data string.
     * @param data Data string in format "colorCode,intensity,bgOnly"
     * @return MoodlightPreset
     */
    private MoodlightPreset generatePreset(String data) {
        if (data == null || data.isEmpty()) {
            return new MoodlightPreset("#000000", 255, false);
        }
        
        String[] bits = data.split(",");
        if (bits.length < 3) {
            return new MoodlightPreset("#000000", 255, false);
        }
        
        String colorCode = bits[0];
        if (!isValidColor(colorCode)) {
            colorCode = "#000000";
        }
        
        int intensity;
        try {
            intensity = Integer.parseInt(bits[1]);
        } catch (NumberFormatException e) {
            intensity = 255;
        }
        
        boolean bgOnly = "1".equals(bits[2]);
        
        return new MoodlightPreset(colorCode, intensity, bgOnly);
    }
    
    /**
     * Gets a preset by number (1, 2, or 3).
     * @param presetNum Preset number (1-based)
     * @return MoodlightPreset
     */
    public MoodlightPreset getPreset(int presetNum) {
        int index = presetNum - 1;
        if (index >= 0 && index < presets.size()) {
            return presets.get(index);
        }
        return new MoodlightPreset("#000000", 255, false);
    }
    
    /**
     * Validates a color code.
     * @param colorCode Color code to validate
     * @return True if valid
     */
    public boolean isValidColor(String colorCode) {
        if (colorCode == null) {
            return false;
        }
        
        return switch (colorCode) {
            case "#000000", "#0053F7", "#EA4532", "#82F349", "#74F5F5", "#E759DE", "#F2F851" -> true;
            default -> false;
        };
    }
    
    /**
     * Validates an intensity value.
     * @param intensity Intensity (0-255)
     * @return True if valid
     */
    public boolean isValidIntensity(int intensity) {
        return intensity >= 0 && intensity <= 255;
    }
    
    /**
     * Generates extra data string for the moodlight item.
     * @return Extra data string
     */
    public String generateExtraData() {
        MoodlightPreset preset = getPreset(currentPreset);
        StringBuilder sb = new StringBuilder();
        
        sb.append(enabled ? 2 : 1);
        sb.append(",");
        sb.append(currentPreset);
        sb.append(",");
        sb.append(preset.isBackgroundOnly() ? 2 : 1);
        sb.append(",");
        sb.append(preset.getColorCode());
        sb.append(",");
        sb.append(preset.getColorIntensity());
        
        return sb.toString();
    }
    
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getCurrentPreset() { return currentPreset; }
    public void setCurrentPreset(int currentPreset) { this.currentPreset = currentPreset; }
    public List<MoodlightPreset> getPresets() { return new ArrayList<>(presets); }
    public long getItemId() { return itemId; }
}
