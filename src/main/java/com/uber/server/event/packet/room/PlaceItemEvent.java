package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client places an item in a room (packet ID 90).
 */
public class PlaceItemEvent extends PacketReceiveEvent {
    private long itemId;
    private int x;
    private int y;
    private int rotation;
    
    public PlaceItemEvent(GameClient client, ClientMessage message, long itemId, int x, int y, int rotation) {
        super(client, message, 90);
        this.itemId = itemId;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
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
    
    public int getRotation() {
        return rotation;
    }
    
    public void setRotation(int rotation) {
        this.rotation = rotation;
    }
}
