package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for TradeClosedEvent (ID 110).
 * Sends trade closed notification.
 */
public class TradingCloseComposer extends OutgoingMessageComposer {
    private final long userId;
    
    public TradingCloseComposer(long userId) {
        this.userId = userId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(110); // _events[110] = TradeClosedEvent
        msg.appendUInt(userId);
        return msg;
    }
}
