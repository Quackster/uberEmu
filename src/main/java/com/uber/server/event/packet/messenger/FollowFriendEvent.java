package com.uber.server.event.packet.messenger;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client follows a friend (packet ID 262).
 */
public class FollowFriendEvent extends PacketReceiveEvent {
    private long userId;
    
    public FollowFriendEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 262);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
