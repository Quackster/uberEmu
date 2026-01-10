package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Event fired when a client edits a room event (packet ID 348).
 */
public class EditEventEvent extends PacketReceiveEvent {
    private int eventId;
    private String name;
    private String description;
    private int category;
    private List<String> tags;
    
    public EditEventEvent(GameClient client, ClientMessage message, int eventId, String name, String description, int category, List<String> tags) {
        super(client, message, 348);
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }
    
    public int getEventId() {
        return eventId;
    }
    
    public void setEventId(int eventId) {
        this.eventId = eventId;
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
    
    public int getCategory() {
        return category;
    }
    
    public void setCategory(int category) {
        this.category = category;
    }
    
    public List<String> getTags() {
        return new ArrayList<>(tags);
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }
}
