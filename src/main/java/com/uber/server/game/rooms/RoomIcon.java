package com.uber.server.game.rooms;

import com.uber.server.messages.ServerMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a room icon.
 */
public class RoomIcon {
    private int backgroundImage;
    private int foregroundImage;
    private final Map<Integer, Integer> items;
    
    public RoomIcon(int backgroundImage, int foregroundImage, Map<Integer, Integer> items) {
        this.backgroundImage = backgroundImage;
        this.foregroundImage = foregroundImage;
        this.items = items != null ? new ConcurrentHashMap<>(items) : new ConcurrentHashMap<>();
    }
    
    /**
     * Creates a RoomIcon from icon data string (format: "pos,item|pos,item|...").
     * @param background Background icon ID
     * @param foreground Foreground icon ID
     * @param itemsStr Items string from database
     * @return RoomIcon object
     */
    public static RoomIcon fromString(int background, int foreground, String itemsStr) {
        Map<Integer, Integer> itemsMap = new ConcurrentHashMap<>();
        
        if (itemsStr != null && !itemsStr.isEmpty()) {
            String[] parts = itemsStr.split("\\|");
            for (String part : parts) {
                String[] itemParts = part.split(",");
                if (itemParts.length == 2) {
                    try {
                        int pos = Integer.parseInt(itemParts[0]);
                        int item = Integer.parseInt(itemParts[1]);
                        itemsMap.put(pos, item);
                    } catch (NumberFormatException e) {
                        // Skip invalid entries
                    }
                }
            }
        }
        
        return new RoomIcon(background, foreground, itemsMap);
    }
    
    /**
     * Serializes the room icon to a ServerMessage.
     * @param message ServerMessage to append to
     */
    public void serialize(ServerMessage message) {
        message.appendInt32(backgroundImage);
        message.appendInt32(foregroundImage);
        message.appendInt32(items.size());
        
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            message.appendInt32(entry.getKey()); // Position
            message.appendInt32(entry.getValue()); // Item ID
        }
    }
    
    // Getters and setters
    public int getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(int backgroundImage) { this.backgroundImage = backgroundImage; }
    public int getForegroundImage() { return foregroundImage; }
    public void setForegroundImage(int foregroundImage) { this.foregroundImage = foregroundImage; }
    public Map<Integer, Integer> getItems() { return new ConcurrentHashMap<>(items); }
}
