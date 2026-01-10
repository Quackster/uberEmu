package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client takes room rights (packet ID 97).
 */
public class TakeRightsEvent extends PacketReceiveEvent {
    private long userId;
    
    public TakeRightsEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 97);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
