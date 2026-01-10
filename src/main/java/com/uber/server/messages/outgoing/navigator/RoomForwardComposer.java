package com.uber.server.messages.outgoing.navigator;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomForwardMessageEvent (ID 286).
 * Sends room forward information to the client.
 */
public class RoomForwardComposer extends OutgoingMessageComposer {
    private final boolean isPublicRoom;
    private final long roomId;
    
    public RoomForwardComposer(boolean isPublicRoom, long roomId) {
        this.isPublicRoom = isPublicRoom;
        this.roomId = roomId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(286); // _events[286] = RoomForwardMessageEvent
        msg.appendBoolean(isPublicRoom);
        msg.appendUInt(roomId);
        return msg;
    }
}
