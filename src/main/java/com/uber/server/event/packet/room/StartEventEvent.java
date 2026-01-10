package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Event fired when a client starts a room event (packet ID 346).
 */
public class StartEventEvent extends PacketReceiveEvent {
    private int eventId;
    private int category;
    private String name;
    private String description;
    private List<String> tags;
    
    public StartEventEvent(GameClient client, ClientMessage message, int eventId, int category, String name, String description, List<String> tags) {
        super(client, message, 346);
        this.eventId = eventId;
        this.category = category;
        this.name = name;
        this.description = description;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }
    
    public int getEventId() {
        return eventId;
    }
    
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    
    public int getCategory() {
        return category;
    }
    
    public void setCategory(int category) {
        this.category = category;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getTags() {
        return new ArrayList<>(tags);
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }
}
