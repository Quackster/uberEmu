package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for TradeUpdateEvent (ID 108).
 * Sends trade window update to both users.
 * Note: This composer wraps complex serialization from Trade.updateTradeWindow().
 */
public class TradingItemListComposer extends OutgoingMessageComposer {
    private final ServerMessage message;
    
    /**
     * Creates a composer with a pre-built message.
     * This is used when the message body is built by Trade.updateTradeWindow().
     */
    public TradingItemListComposer(ServerMessage message) {
        this.message = message;
    }
    
    @Override
    public ServerMessage compose() {
        return message;
    }
}
