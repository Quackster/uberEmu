package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator sends a room alert (packet ID 200).
 */
public class ModSendRoomAlertEvent extends PacketReceiveEvent {
    private int roomId;
    private String message;
    
    public ModSendRoomAlertEvent(GameClient client, ClientMessage message, int roomId, String alertMessage) {
        super(client, message, 200);
        this.roomId = roomId;
        this.message = alertMessage;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
