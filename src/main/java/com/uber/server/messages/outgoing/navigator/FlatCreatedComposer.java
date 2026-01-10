package com.uber.server.messages.outgoing.navigator;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for FlatCreatedEvent (ID 59).
 * Sends room creation confirmation to the client.
 */
public class FlatCreatedComposer extends OutgoingMessageComposer {
    private final long roomId;
    private final String roomName;
    
    public FlatCreatedComposer(long roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(59); // _events[59] = FlatCreatedEvent
        msg.appendUInt(roomId);
        msg.appendStringWithBreak(roomName);
        return msg;
    }
}
