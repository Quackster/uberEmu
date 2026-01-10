package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client sends a whisper message (packet ID 56).
 */
public class WhisperMessageEvent extends PacketReceiveEvent {
    private String message;
    private String targetUsername;
    
    public WhisperMessageEvent(GameClient client, ClientMessage message, String whisperMessage, String targetUsername) {
        super(client, message, 56);
        this.message = whisperMessage;
        this.targetUsername = targetUsername;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTargetUsername() {
        return targetUsername;
    }
    
    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }
}
