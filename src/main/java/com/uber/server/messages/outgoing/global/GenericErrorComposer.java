package com.uber.server.messages.outgoing.global;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for GenericErrorEvent (ID 33).
 * Sends a generic error message to the client.
 */
public class GenericErrorComposer extends OutgoingMessageComposer {
    private final int errorCode;
    
    public GenericErrorComposer(int errorCode) {
        this.errorCode = errorCode;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(33); // _events[33] = GenericErrorEvent
        msg.appendInt32(errorCode);
        return msg;
    }
}
