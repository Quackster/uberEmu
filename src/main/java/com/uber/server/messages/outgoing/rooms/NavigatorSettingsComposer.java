package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for HomeRoomMessageEvent (ID 455).
 * Confirms home room was set.
 */
public class NavigatorSettingsComposer extends OutgoingMessageComposer {
    private final long roomId;
    
    public NavigatorSettingsComposer(long roomId) {
        this.roomId = roomId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(455); // _events[455] = HomeRoomMessageEvent
        msg.appendUInt(roomId);
        return msg;
    }
}
