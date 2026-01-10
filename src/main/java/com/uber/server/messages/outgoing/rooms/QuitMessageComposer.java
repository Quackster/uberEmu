package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for QuitMessageEvent (ID 53).
 * Sends quit/leave room message to the client.
 */
public class QuitMessageComposer extends OutgoingMessageComposer {
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(53); // _events[53] = QuitMessageEvent
        // Empty message body
        return msg;
    }
}
