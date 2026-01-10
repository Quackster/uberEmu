package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client initiates a trade (packet ID 71).
 */
public class InitTradeEvent extends PacketReceiveEvent {
    private long targetUserId;
    
    public InitTradeEvent(GameClient client, ClientMessage message, long targetUserId) {
        super(client, message, 71);
        this.targetUserId = targetUserId;
    }
    
    public long getTargetUserId() {
        return targetUserId;
    }
    
    public void setTargetUserId(long targetUserId) {
        this.targetUserId = targetUserId;
    }
}
