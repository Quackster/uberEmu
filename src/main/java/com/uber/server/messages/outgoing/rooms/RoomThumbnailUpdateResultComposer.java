package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomIconSavedMessageEvent (ID 457).
 * Confirms room icon was saved.
 */
public class RoomThumbnailUpdateResultComposer extends OutgoingMessageComposer {
    private final long roomId;
    private final boolean success;
    
    public RoomThumbnailUpdateResultComposer(long roomId, boolean success) {
        this.roomId = roomId;
        this.success = success;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(457); // _events[457] = RoomIconSavedMessageEvent
        msg.appendUInt(roomId);
        msg.appendBoolean(success);
        return msg;
    }
}
