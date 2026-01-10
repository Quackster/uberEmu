package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for TradeCompleteEvent (ID 112).
 * Sent when a trade is completed.
 */
public class TradeCompleteEventComposer extends OutgoingMessageComposer {
    
    public TradeCompleteEventComposer() {
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(112);
        return msg;
    }
}
