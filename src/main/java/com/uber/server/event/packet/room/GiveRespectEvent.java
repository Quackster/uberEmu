package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client gives respect (packet ID 371).
 */
public class GiveRespectEvent extends PacketReceiveEvent {
    private long userId;
    
    public GiveRespectEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 371);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
