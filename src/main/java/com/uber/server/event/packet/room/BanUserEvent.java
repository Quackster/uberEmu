package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client bans a user from a room (packet ID 320).
 */
public class BanUserEvent extends PacketReceiveEvent {
    private long userId;
    
    public BanUserEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 320);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
