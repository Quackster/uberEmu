package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomRightsGivenToUserMessageEvent (ID 42).
 * Notifies user that they were given room rights.
 */
public class YouAreControllerComposer extends OutgoingMessageComposer {
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(42); // _events[42] = RoomRightsGivenToUserMessageEvent
        // Empty message body
        return msg;
    }
}
