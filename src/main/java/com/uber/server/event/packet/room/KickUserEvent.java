package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client kicks a user from a room (packet ID 95).
 */
public class KickUserEvent extends PacketReceiveEvent {
    private long userId;
    
    public KickUserEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 95);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
