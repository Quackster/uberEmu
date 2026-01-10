package com.uber.server.messages.outgoing.global;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for GenericErrorEvent (ID 33).
 * Sent for generic error messages.
 */
public class GenericErrorEventComposer extends OutgoingMessageComposer {
    private final int errorCode;
    
    public GenericErrorEventComposer(int errorCode) {
        this.errorCode = errorCode;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(33);
        msg.appendInt32(errorCode);
        return msg;
    }
}
