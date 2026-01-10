package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator sends a user caution (packet ID 461).
 */
public class ModSendUserCautionEvent extends PacketReceiveEvent {
    private long userId;
    private String message;
    
    public ModSendUserCautionEvent(GameClient client, ClientMessage message, long userId, String cautionMessage) {
        super(client, message, 461);
        this.userId = userId;
        this.message = cautionMessage;
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
