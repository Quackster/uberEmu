package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client calls for help (packet ID 453).
 */
public class CallForHelpEvent extends PacketReceiveEvent {
    private String message;
    private int categoryId;
    private int roomId;
    
    public CallForHelpEvent(GameClient client, ClientMessage message, String messageText, int categoryId, int roomId) {
        super(client, message, 453);
        this.message = messageText;
        this.categoryId = categoryId;
        this.roomId = roomId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
