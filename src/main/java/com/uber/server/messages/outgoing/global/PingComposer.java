package com.uber.server.messages.outgoing.global;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for PingMessageEvent (ID 50).
 * Sends ping/keepalive message to the client.
 */
public class PingComposer extends OutgoingMessageComposer {
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(50); // _events[50] = PingMessageEvent
        // Empty message body
        return msg;
    }
}
