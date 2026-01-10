package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests to load room for user (packet ID 59).
 */
public class ReqLoadRoomForUserEvent extends PacketReceiveEvent {
    private long userId;
    
    public ReqLoadRoomForUserEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 59);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
