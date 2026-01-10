package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for OpenConnectionMessageEvent (ID 2).
 * Sends public room entry connection message to the client.
 */
public class OpenConnectionMessageComposer extends OutgoingMessageComposer {
    private final String modelPath;
    
    public OpenConnectionMessageComposer(String modelPath) {
        this.modelPath = modelPath;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(2); // _events[2] = OpenConnectionMessageEvent
        msg.appendStringWithBreak(modelPath);
        return msg;
    }
}
