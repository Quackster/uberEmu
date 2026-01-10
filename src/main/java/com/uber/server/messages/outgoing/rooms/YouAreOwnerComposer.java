package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomOwnerMessageEvent (ID 47).
 * Sends room owner message to the client (user is owner).
 */
public class YouAreOwnerComposer extends OutgoingMessageComposer {
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(47); // _events[47] = RoomOwnerMessageEvent
        // Empty message body
        return msg;
    }
}
