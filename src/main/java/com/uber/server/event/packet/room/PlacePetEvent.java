package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client places a pet (packet ID 3002).
 */
public class PlacePetEvent extends PacketReceiveEvent {
    private long petId;
    private int x;
    private int y;
    
    public PlacePetEvent(GameClient client, ClientMessage message, long petId, int x, int y) {
        super(client, message, 3002);
        this.petId = petId;
        this.x = x;
        this.y = y;
    }
    
    public long getPetId() {
        return petId;
    }
    
    public void setPetId(long petId) {
        this.petId = petId;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
}
