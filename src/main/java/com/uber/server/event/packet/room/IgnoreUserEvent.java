package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client ignores a user (packet ID 319).
 */
public class IgnoreUserEvent extends PacketReceiveEvent {
    private String username;
    
    public IgnoreUserEvent(GameClient client, ClientMessage message, String username) {
        super(client, message, 319);
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}
