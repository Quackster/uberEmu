package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for DanceMessageEvent (ID 480).
 * Sends dance animation to clients in the room.
 */
public class DanceMessageComposer extends OutgoingMessageComposer {
    private final int virtualId;
    private final int danceId;
    
    public DanceMessageComposer(int virtualId, int danceId) {
        this.virtualId = virtualId;
        this.danceId = danceId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(480); // _events[480] = DanceMessageEvent
        msg.appendInt32(virtualId);
        msg.appendInt32(danceId);
        return msg;
    }
}
