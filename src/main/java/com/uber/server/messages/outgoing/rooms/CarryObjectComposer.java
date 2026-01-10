package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for UserCarryItemMessageEvent (ID 482).
 * Sends user carrying item status to the client.
 */
public class CarryObjectComposer extends OutgoingMessageComposer {
    private final int virtualId;
    private final int carryTimer;
    
    public CarryObjectComposer(int virtualId, int carryTimer) {
        this.virtualId = virtualId;
        this.carryTimer = carryTimer;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(482); // _events[482] = UserCarryItemMessageEvent
        msg.appendInt32(virtualId);
        msg.appendInt32(carryTimer);
        return msg;
    }
}
