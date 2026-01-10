package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for CantConnectMessageEvent (ID 224).
 * Sent when user cannot connect to a room.
 * Error codes: 1 = Room full, 4 = Banned
 */
public class CantConnectMessageEventComposer extends OutgoingMessageComposer {
    private final int errorCode;
    
    public CantConnectMessageEventComposer(int errorCode) {
        this.errorCode = errorCode;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(224);
        msg.appendInt32(errorCode);
        return msg;
    }
}
