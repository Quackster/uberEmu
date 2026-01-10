package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomSettingsUpdatedMessageEvent (ID 456).
 * Notifies client that room settings were updated.
 */
public class RoomInfoUpdatedComposer extends OutgoingMessageComposer {
    private final long roomId;
    
    public RoomInfoUpdatedComposer(long roomId) {
        this.roomId = roomId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(456); // _events[456] = RoomSettingsUpdatedMessageEvent
        msg.appendUInt(roomId);
        return msg;
    }
}
