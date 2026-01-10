package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for UserCarryItemMessageEvent (ID 482).
 * Sent when a user is carrying an item.
 */
public class UserCarryItemMessageEventComposer extends OutgoingMessageComposer {
    private final int virtualId;
    private final int carryTimer;
    
    public UserCarryItemMessageEventComposer(int virtualId, int carryTimer) {
        this.virtualId = virtualId;
        this.carryTimer = carryTimer;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(482);
        msg.appendInt32(virtualId);
        msg.appendInt32(carryTimer);
        return msg;
    }
}
