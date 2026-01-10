package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator requests user chatlog (packet ID 455).
 */
public class ModGetUserChatlogEvent extends PacketReceiveEvent {
    private long userId;
    
    public ModGetUserChatlogEvent(GameClient client, ClientMessage message, long userId) {
        super(client, message, 455);
        this.userId = userId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
