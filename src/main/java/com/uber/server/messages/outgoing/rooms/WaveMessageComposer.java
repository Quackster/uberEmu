package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for WaveMessageEvent (ID 481).
 * Sends wave animation to clients in the room.
 */
public class WaveMessageComposer extends OutgoingMessageComposer {
    private final int virtualId;
    
    public WaveMessageComposer(int virtualId) {
        this.virtualId = virtualId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(481); // _events[481] = WaveMessageEvent
        msg.appendInt32(virtualId);
        return msg;
    }
}
