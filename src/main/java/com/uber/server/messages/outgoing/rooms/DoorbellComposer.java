package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for DoorbellMessageEvent (ID 91).
 * Sends doorbell ring message to the client.
 */
public class DoorbellComposer extends OutgoingMessageComposer {
    private final String username;
    
    public DoorbellComposer(String username) {
        this.username = username != null ? username : "";
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(91); // _events[91] = DoorbellMessageEvent
        msg.appendStringWithBreak(username);
        return msg;
    }
}
