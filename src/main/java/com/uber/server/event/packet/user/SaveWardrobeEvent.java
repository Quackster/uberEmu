package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client saves wardrobe (packet ID 376).
 */
public class SaveWardrobeEvent extends PacketReceiveEvent {
    private int slotId;
    private String figure;
    private String gender;
    
    public SaveWardrobeEvent(GameClient client, ClientMessage message, int slotId, String figure, String gender) {
        super(client, message, 376);
        this.slotId = slotId;
        this.figure = figure;
        this.gender = gender;
    }
    
    public int getSlotId() {
        return slotId;
    }
    
    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }
    
    public String getFigure() {
        return figure;
    }
    
    public void setFigure(String figure) {
        this.figure = figure;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
}
