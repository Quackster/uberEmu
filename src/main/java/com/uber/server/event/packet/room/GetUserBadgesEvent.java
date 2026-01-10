package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests user badges (packet ID 159).
 */
public class GetUserBadgesEvent extends PacketReceiveEvent {
    private long userId;
    
    public GetUserBadgesEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 159);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
