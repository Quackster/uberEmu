package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for CanCreateRoomEventResponseMessageEvent (ID 367).
 * Response to can create room event check.
 */
public class CanCreateRoomEventComposer extends OutgoingMessageComposer {
    private final boolean allow;
    private final int errorCode;
    
    public CanCreateRoomEventComposer(boolean allow, int errorCode) {
        this.allow = allow;
        this.errorCode = errorCode;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(367); // _events[367] = CanCreateRoomEventResponseMessageEvent
        msg.appendBoolean(allow);
        msg.appendInt32(errorCode);
        return msg;
    }
}
