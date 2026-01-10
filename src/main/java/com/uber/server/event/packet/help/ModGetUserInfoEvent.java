package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator requests user info (packet ID 454).
 */
public class ModGetUserInfoEvent extends PacketReceiveEvent {
    private long userId;
    
    public ModGetUserInfoEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 454);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
