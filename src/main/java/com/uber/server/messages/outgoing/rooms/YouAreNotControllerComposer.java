package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomRightsRemovedFromUserMessageEvent (ID 43).
 * Notifies user that their room rights were removed.
 */
public class YouAreNotControllerComposer extends OutgoingMessageComposer {
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(43); // _events[43] = RoomRightsRemovedFromUserMessageEvent
        // Empty message body
        return msg;
    }
}
