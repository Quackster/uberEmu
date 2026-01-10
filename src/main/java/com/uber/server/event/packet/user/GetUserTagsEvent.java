package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests user tags (packet ID 263).
 */
public class GetUserTagsEvent extends PacketReceiveEvent {
    private long userId;
    
    public GetUserTagsEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 263);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
