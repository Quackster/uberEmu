package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client creates a flat/room (packet ID 29).
 */
public class CreateFlatEvent extends PacketReceiveEvent {
    private String name;
    private String description;
    private String model;
    private int categoryId;
    private int maxUsers;
    private int tradeMode;
    
    public CreateFlatEvent(GameClient client, ClientMessage message, String name, String description, 
                          String model, int categoryId, int maxUsers, int tradeMode) {
        super(client, message, 29);
        this.name = name;
        this.description = description;
        this.model = model;
        this.categoryId = categoryId;
        this.maxUsers = maxUsers;
        this.tradeMode = tradeMode;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    
    public int getMaxUsers() { return maxUsers; }
    public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }
    
    public int getTradeMode() { return tradeMode; }
    public void setTradeMode(int tradeMode) { this.tradeMode = tradeMode; }
}
