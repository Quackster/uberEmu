package com.uber.server.event.packet.messenger;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client sends an instant message (packet ID 33).
 */
public class SendMsgEvent extends PacketReceiveEvent {
    private long userId;
    private String message;
    
    public SendMsgEvent(GameClient client, ClientMessage message, long userId, String messageText) {
        super(client, message, 33);
        this.userId = userId;
        this.message = messageText;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
