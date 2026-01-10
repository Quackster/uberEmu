package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomRightsRemovedMessageEvent (ID 511).
 * Notifies client that room rights were removed from a user.
 */
public class FlatControllerRemovedComposer extends OutgoingMessageComposer {
    private final long roomId;
    private final long userId;
    
    public FlatControllerRemovedComposer(long roomId, long userId) {
        this.roomId = roomId;
        this.userId = userId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(511); // _events[511] = RoomRightsRemovedMessageEvent
        msg.appendUInt(roomId);
        msg.appendUInt(userId);
        return msg;
    }
}
