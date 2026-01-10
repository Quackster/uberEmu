package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for OpenFlatConnectionMessageEvent (ID 391).
 * Sends private room entry connection message to the client.
 */
public class OpenFlatConnectionComposer extends OutgoingMessageComposer {
    private final String roomPath;
    
    public OpenFlatConnectionComposer(String roomPath) {
        this.roomPath = roomPath;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(391); // _events[391] = OpenFlatConnectionMessageEvent
        msg.appendStringWithBreak(roomPath);
        return msg;
    }
}
