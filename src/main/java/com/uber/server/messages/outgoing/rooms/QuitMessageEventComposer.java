package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for QuitMessageEvent (ID 18).
 * Sent to quit/close room entry.
 */
public class QuitMessageEventComposer extends OutgoingMessageComposer {
    
    public QuitMessageEventComposer() {
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(18);
        return msg;
    }
}
