package com.uber.server.game.rooms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents room data/metadata.
 */
public class RoomData {
    private long id;
    private String name;
    private String description;
    private String type;
    private String owner;
    private String password;
    private int state; // 0 = open, 1 = locked, 2 = password
    private int category;
    private int usersNow;
    private int usersMax;
    private String modelName;
    private String ccts;
    private int score;
    private List<String> tags;
    private boolean allowPets;
    private boolean allowPetsEating;
    private boolean allowWalkthrough;
    private String wallpaper;
    private String floor;
    private String landscape;
    private RoomIcon icon;
    
    public RoomData() {
        this.tags = new ArrayList<>();
        this.icon = new RoomIcon(1, 1, new java.util.concurrent.ConcurrentHashMap<>());
    }
    
    /**
     * Fills room data from database row.
     */
    public void fill(Map<String, Object> row) {
        this.id = ((Number) row.get("id")).longValue();
        this.name = (String) row.get("caption");
        this.description = (String) row.get("description");
        this.type = (String) row.get("roomtype");
        this.owner = (String) row.get("owner");
        
        String stateStr = ((String) row.get("state")).toLowerCase();
        this.state = switch (stateStr) {
            case "open" -> 0;
            case "password" -> 2;
            case "locked" -> 1;
            default -> 1;
        };
        
        this.category = ((Number) row.get("category")).intValue();
        this.usersNow = ((Number) row.get("users_now")).intValue();
        this.usersMax = ((Number) row.get("users_max")).intValue();
        this.modelName = (String) row.get("model_name");
        this.ccts = row.get("public_ccts") != null ? (String) row.get("public_ccts") : "";
        this.score = ((Number) row.get("score")).intValue();
        this.tags = new ArrayList<>();
        
        // Parse tags if present
        String tagsStr = (String) row.get("tags");
        if (tagsStr != null && !tagsStr.isEmpty()) {
            String[] tagArray = tagsStr.split(",");
            for (String tag : tagArray) {
                if (!tag.trim().isEmpty()) {
                    this.tags.add(tag.trim());
                }
            }
        }
        
        this.allowPets = parseBoolean(row.get("allow_pets"));
        this.allowPetsEating = parseBoolean(row.get("allow_pets_eat"));
        this.allowWalkthrough = parseBoolean(row.get("allow_walkthrough"));
        this.password = row.get("password") != null ? (String) row.get("password") : null;
        this.wallpaper = row.get("wallpaper") != null ? (String) row.get("wallpaper") : "0.0";
        this.floor = row.get("floor") != null ? (String) row.get("floor") : "0.0";
        this.landscape = row.get("landscape") != null ? (String) row.get("landscape") : "0.0";
        
        // Load room icon
        int iconBg = row.get("icon_bg") != null ? ((Number) row.get("icon_bg")).intValue() : 1;
        int iconFg = row.get("icon_fg") != null ? ((Number) row.get("icon_fg")).intValue() : 1;
        String iconItems = row.get("icon_items") != null ? (String) row.get("icon_items") : "";
        this.icon = RoomIcon.fromString(iconBg, iconFg, iconItems);
    }
    
    /**
     * Fills with null/default values.
     */
    public void fillNull(long id) {
        this.id = id;
        this.name = "Unknown Room";
        this.description = "-";
        this.type = "private";
        this.owner = "-";
        this.category = 0;
        this.usersNow = 0;
        this.usersMax = 0;
        this.modelName = "NO_MODEL";
        this.ccts = "";
        this.score = 0;
        this.tags = new ArrayList<>();
        this.allowPets = true;
        this.allowPetsEating = false;
        this.allowWalkthrough = true;
        this.password = "";
        this.wallpaper = "0.0";
        this.floor = "0.0";
        this.landscape = "0.0";
        this.icon = new RoomIcon(1, 1, new java.util.concurrent.ConcurrentHashMap<>());
    }
    
    private boolean parseBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String str = value.toString().trim();
        return "1".equals(str) || "true".equalsIgnoreCase(str);
    }
    
    public boolean isPublicRoom() {
        return "public".equalsIgnoreCase(type);
    }
    
    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getOwner() { return owner; }
    public String getPassword() { return password; }
    public int getState() { return state; }
    public int getCategory() { return category; }
    public int getUsersNow() { return usersNow; }
    public int getUsersMax() { return usersMax; }
    public String getModelName() { return modelName; }
    public String getCCTs() { return ccts; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public List<String> getTags() { return tags; }
    public boolean isAllowPets() { return allowPets; }
    public boolean isAllowPetsEating() { return allowPetsEating; }
    public boolean isAllowWalkthrough() { return allowWalkthrough; }
    public String getWallpaper() { return wallpaper; }
    public String getFloor() { return floor; }
    public String getLandscape() { return landscape; }
    
    public int getTagCount() { return tags.size(); }
    
    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPassword(String password) { this.password = password; }
    public void setState(int state) { this.state = state; }
    public void setCategory(int category) { this.category = category; }
    public void setUsersMax(int usersMax) { this.usersMax = usersMax; }
    public void setTags(List<String> tags) { 
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>(); 
    }
    public void setAllowPets(boolean allowPets) { this.allowPets = allowPets; }
    public void setAllowPetsEating(boolean allowPetsEating) { this.allowPetsEating = allowPetsEating; }
    public void setAllowWalkthrough(boolean allowWalkthrough) { this.allowWalkthrough = allowWalkthrough; }
    public void setWallpaper(String wallpaper) { this.wallpaper = wallpaper != null ? wallpaper : "0.0"; }
    public void setFloor(String floor) { this.floor = floor != null ? floor : "0.0"; }
    public void setLandscape(String landscape) { this.landscape = landscape != null ? landscape : "0.0"; }
    public RoomIcon getIcon() { return icon; }
    public void setIcon(RoomIcon icon) { this.icon = icon; }
    
    /**
     * Serializes room data to a ServerMessage.
     * @param message ServerMessage to append to
     * @param showEvents Whether to show room events
     * @param event Optional room event (if showEvents is true and event exists)
     */
    public void serialize(com.uber.server.messages.ServerMessage message, boolean showEvents, RoomEvent event) {
        message.appendUInt(id);
        
        if (event == null || !showEvents) {
            // Standard room serialization
            message.appendBoolean(false); // Has event
            message.appendStringWithBreak(name != null ? name : "");
            message.appendStringWithBreak(owner != null ? owner : "");
            message.appendInt32(state); // Room state
            message.appendInt32(usersNow);
            message.appendInt32(usersMax);
            message.appendStringWithBreak(description != null ? description : "");
            message.appendBoolean(true); // Unknown flag
            message.appendBoolean(true); // Can trade?
            message.appendInt32(score);
            message.appendInt32(category);
            message.appendStringWithBreak(""); // Event start time (empty for non-events)
            message.appendInt32(getTagCount());
            
            for (String tag : tags) {
                message.appendStringWithBreak(tag != null ? tag : "");
            }
        } else {
            // Event serialization
            message.appendBoolean(true); // Has event
            message.appendStringWithBreak(event.getName() != null ? event.getName() : "");
            message.appendStringWithBreak(owner != null ? owner : "");
            message.appendInt32(state);
            message.appendInt32(usersNow);
            message.appendInt32(usersMax);
            message.appendStringWithBreak(event.getDescription() != null ? event.getDescription() : "");
            message.appendBoolean(true); // Unknown flag
            message.appendBoolean(true); // Can trade?
            message.appendInt32(score);
            message.appendInt32(event.getCategory());
            message.appendStringWithBreak(event.getStartTime() != null ? event.getStartTime() : "");
            message.appendInt32(event.getTags().size());
            
            for (String tag : event.getTags()) {
                message.appendStringWithBreak(tag != null ? tag : "");
            }
        }
        
        // Serialize icon
        if (icon != null) {
            icon.serialize(message);
        } else {
            // Default icon if not set
            RoomIcon defaultIcon = new RoomIcon(0, 0, new java.util.concurrent.ConcurrentHashMap<>());
            defaultIcon.serialize(message);
        }
        
        message.appendBoolean(true); // Unknown flag
    }
    
    /**
     * Serializes room data to a ServerMessage (without event).
     * Convenience method for backward compatibility.
     * @param message ServerMessage to append to
     * @param showEvents Whether to show room events (ignored if no event provided)
     */
    public void serialize(com.uber.server.messages.ServerMessage message, boolean showEvents) {
        serialize(message, showEvents, null);
    }
}
