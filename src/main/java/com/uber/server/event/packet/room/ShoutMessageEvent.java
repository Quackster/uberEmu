package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client sends a shout message (packet ID 55).
 */
public class ShoutMessageEvent extends PacketReceiveEvent {
    private String message;
    
    public ShoutMessageEvent(GameClient client, ClientMessage message, String shoutMessage) {
        super(client, message, 55);
        this.message = shoutMessage;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
