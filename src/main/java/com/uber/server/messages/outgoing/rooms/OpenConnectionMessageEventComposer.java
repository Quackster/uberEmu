package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for OpenConnectionMessageEvent (ID 166).
 * Sent to open connection to a room.
 */
public class OpenConnectionMessageEventComposer extends OutgoingMessageComposer {
    private final String path;
    
    public OpenConnectionMessageEventComposer(String path) {
        this.path = path;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(166);
        msg.appendStringWithBreak(path);
        return msg;
    }
}
