package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomEntryErrorMessageEvent (ID 18).
 * Sends room entry error/cancellation message to the client.
 */
public class CloseConnectionComposer extends OutgoingMessageComposer {
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(18); // _events[18] = RoomEntryErrorMessageEvent
        // Empty message body
        return msg;
    }
}
