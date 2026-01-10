package com.uber.server.messages.outgoing.handshake;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for AuthenticationOKMessageEvent (ID 3).
 * Sends authentication success notification to the client.
 */
public class AuthenticationOKComposer extends OutgoingMessageComposer {
    private final String message;
    
    public AuthenticationOKComposer(String message) {
        this.message = message != null ? message : "";
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(3); // _events[3] = AuthenticationOKMessageEvent
        msg.appendStringWithBreak(message);
        return msg;
    }
}
