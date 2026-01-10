package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for TradingOpenEvent (ID 104).
 * Sends trade start notification to both users.
 */
public class TradingOpenComposer extends OutgoingMessageComposer {
    private final long userOneId;
    private final boolean userOneAccepted;
    private final long userTwoId;
    private final boolean userTwoAccepted;
    
    public TradingOpenComposer(long userOneId, boolean userOneAccepted, long userTwoId, boolean userTwoAccepted) {
        this.userOneId = userOneId;
        this.userOneAccepted = userOneAccepted;
        this.userTwoId = userTwoId;
        this.userTwoAccepted = userTwoAccepted;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(104); // _events[104] = TradeStartEvent
        msg.appendUInt(userOneId);
        msg.appendBoolean(userOneAccepted);
        msg.appendUInt(userTwoId);
        msg.appendBoolean(userTwoAccepted);
        return msg;
    }
}
