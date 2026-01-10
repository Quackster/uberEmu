package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for TradeCompleteEvent (ID 112).
 * Sends trade completion notification.
 */
public class TradingCompletedComposer extends OutgoingMessageComposer {
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(112); // _events[112] = TradeCompleteEvent
        // Empty message body
        return msg;
    }
}
