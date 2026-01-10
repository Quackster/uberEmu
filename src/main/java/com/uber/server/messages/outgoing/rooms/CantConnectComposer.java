package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for CantConnectMessageEvent (ID 224).
 * Sends room connection error message to the client.
 * Error codes: 1 = Room full, 4 = Banned
 */
public class CantConnectComposer extends OutgoingMessageComposer {
    private final int errorCode;
    
    public CantConnectComposer(int errorCode) {
        this.errorCode = errorCode;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(224); // _events[224] = CantConnectMessageEvent
        msg.appendInt32(errorCode);
        return msg;
    }
}
