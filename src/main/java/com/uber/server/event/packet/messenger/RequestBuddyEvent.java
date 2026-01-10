package com.uber.server.event.packet.messenger;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests a buddy (packet ID 39).
 */
public class RequestBuddyEvent extends PacketReceiveEvent {
    private String username;
    
    public RequestBuddyEvent(GameClient client, ClientMessage message, String username) {
        super(client, message, 39);
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}
