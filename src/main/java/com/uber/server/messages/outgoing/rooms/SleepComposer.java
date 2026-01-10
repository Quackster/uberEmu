package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for UserSleepingMessageEvent (ID 486).
 * Sends user sleeping status to the client.
 */
public class SleepComposer extends OutgoingMessageComposer {
    private final int virtualId;
    private final boolean isAsleep;
    
    public SleepComposer(int virtualId, boolean isAsleep) {
        this.virtualId = virtualId;
        this.isAsleep = isAsleep;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(486); // _events[486] = UserSleepingMessageEvent
        msg.appendInt32(virtualId);
        msg.appendBoolean(isAsleep);
        return msg;
    }
}
