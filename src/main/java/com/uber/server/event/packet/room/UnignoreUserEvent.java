package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client unignores a user (packet ID 322).
 */
public class UnignoreUserEvent extends PacketReceiveEvent {
    private String username;
    
    public UnignoreUserEvent(GameClient client, ClientMessage message, String username) {
        super(client, message, 322);
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}
