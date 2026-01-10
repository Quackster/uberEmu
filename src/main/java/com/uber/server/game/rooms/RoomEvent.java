package com.uber.server.game.rooms;

import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ServerMessage;
import com.uber.server.util.StringUtil;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a room event.
 */
public class RoomEvent {
    private String name;
    private String description;
    private int category;
    private final List<String> tags;
    private String startTime;
    private final long roomId;
    
    public RoomEvent(long roomId, String name, String description, int category) {
        this.roomId = roomId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.tags = new CopyOnWriteArrayList<>();
        this.startTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    /**
     * Serializes the room event to a ServerMessage.
     * @param session GameClient session
     * @return ServerMessage with event data
     */
    public ServerMessage serialize(GameClient session) {
        ServerMessage message = new ServerMessage(370); // _events[370] = RoomEventEvent
        
        Habbo habbo = session != null ? session.getHabbo() : null;
        if (habbo != null) {
            message.appendStringWithBreak(String.valueOf(habbo.getId()));
            message.appendStringWithBreak(habbo.getUsername());
        } else {
            message.appendStringWithBreak("0");
            message.appendStringWithBreak("");
        }
        
        message.appendStringWithBreak(String.valueOf(roomId));
        message.appendInt32(category);
        message.appendStringWithBreak(name);
        message.appendStringWithBreak(description);
        message.appendStringWithBreak(startTime);
        message.appendInt32(tags.size());
        
        for (String tag : tags) {
            message.appendStringWithBreak(tag);
        }
        
        return message;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }
    public List<String> getTags() { return new ArrayList<>(tags); }
    public void setTags(List<String> tags) {
        this.tags.clear();
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }
    public String getStartTime() { return startTime; }
    public long getRoomId() { return roomId; }
}
