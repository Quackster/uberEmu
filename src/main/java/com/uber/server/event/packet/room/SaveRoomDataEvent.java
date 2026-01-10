package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client saves room data (packet ID 401).
 */
public class SaveRoomDataEvent extends PacketReceiveEvent {
    private int roomId;
    private String name;
    private String description;
    private int state;
    private String password;
    private int maxUsers;
    private int categoryId;
    private int tags;
    
    public SaveRoomDataEvent(GameClient client, ClientMessage message, int roomId, String name, 
                            String description, int state, String password, int maxUsers, 
                            int categoryId, int tags) {
        super(client, message, 401);
        this.roomId = roomId;
        this.name = name;
        this.description = description;
        this.state = state;
        this.password = password;
        this.maxUsers = maxUsers;
        this.categoryId = categoryId;
        this.tags = tags;
    }
    
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getState() { return state; }
    public void setState(int state) { this.state = state; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getMaxUsers() { return maxUsers; }
    public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }
    
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    
    public int getTags() { return tags; }
    public void setTags(int tags) { this.tags = tags; }
}
