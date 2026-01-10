package com.uber.server.messages.outgoing.handshake;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for SessionParametersMessageEvent (ID 257).
 * Sends session parameters to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class SessionParamsComposer extends OutgoingMessageComposer {
    private final ServerMessage parametersMessage;
    
    public SessionParamsComposer(ServerMessage parametersMessage) {
        this.parametersMessage = parametersMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return parametersMessage; // Already built with ID 257
    }
}
