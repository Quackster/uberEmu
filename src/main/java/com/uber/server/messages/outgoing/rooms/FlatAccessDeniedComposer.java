package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for FlatAccessDeniedMessageEvent (ID 131).
 * Sends flat/room access denied message to the client.
 */
public class FlatAccessDeniedComposer extends OutgoingMessageComposer {
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(131); // _events[131] = FlatAccessDeniedMessageEvent
        // Empty message body
        return msg;
    }
}
