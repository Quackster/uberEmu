package com.uber.server.event.packet.messenger;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client removes a buddy (packet ID 40).
 */
public class RemoveBuddyEvent extends PacketReceiveEvent {
    private long userId;
    
    public RemoveBuddyEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 40);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
