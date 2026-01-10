package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for TradingAcceptEvent (ID 109).
 * Sends trade acceptance status update.
 */
public class TradingAcceptComposer extends OutgoingMessageComposer {
    private final long userId;
    private final boolean accepted;
    
    public TradingAcceptComposer(long userId, boolean accepted) {
        this.userId = userId;
        this.accepted = accepted;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(109); // _events[109] = TradeAcceptEvent
        msg.appendUInt(userId);
        msg.appendBoolean(accepted);
        return msg;
    }
}
