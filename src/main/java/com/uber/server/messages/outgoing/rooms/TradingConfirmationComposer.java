package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for TradeConfirmationEvent (ID 111).
 * Sends trade confirmation when all users have accepted.
 */
public class TradingConfirmationComposer extends OutgoingMessageComposer {
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(111); // _events[111] = TradeConfirmationEvent
        // Empty message body
        return msg;
    }
}
