package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client gives room rights (packet ID 96).
 */
public class GiveRightsEvent extends PacketReceiveEvent {
    private long userId;
    
    public GiveRightsEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 96);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
