package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomSettingsSavedMessageEvent (ID 467).
 * Confirms room settings were saved.
 */
public class RoomSettingsSavedComposer extends OutgoingMessageComposer {
    private final long roomId;
    
    public RoomSettingsSavedComposer(long roomId) {
        this.roomId = roomId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(467); // _events[467] = RoomSettingsSavedMessageEvent
        msg.appendUInt(roomId);
        return msg;
    }
}
