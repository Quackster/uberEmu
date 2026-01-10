package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator kicks a user (packet ID 463).
 */
public class ModKickUserEvent extends PacketReceiveEvent {
    private long userId;
    private String message;
    
    public ModKickUserEvent(GameClient client, ClientMessage message, long userId, String kickMessage) {
        super(client, message, 463);
        this.userId = userId;
        this.message = kickMessage;
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
