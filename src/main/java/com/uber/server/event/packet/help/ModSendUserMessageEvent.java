package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator sends a user message (packet ID 462).
 */
public class ModSendUserMessageEvent extends PacketReceiveEvent {
    private long userId;
    private String message;
    
    public ModSendUserMessageEvent(GameClient client, ClientMessage message, long userId, String userMessage) {
        super(client, message, 462);
        this.userId = userId;
        this.message = userMessage;
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
