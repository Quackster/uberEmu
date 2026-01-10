package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client saves a postit (packet ID 84).
 */
public class SavePostitEvent extends PacketReceiveEvent {
    private long itemId;
    private String text;
    private String color;
    
    public SavePostitEvent(GameClient client, ClientMessage message, long itemId, String text, String color) {
        super(client, message, 84);
        this.itemId = itemId;
        this.text = text;
        this.color = color;
    }
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
}
