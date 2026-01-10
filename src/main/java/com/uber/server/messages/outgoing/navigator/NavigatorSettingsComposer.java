package com.uber.server.messages.outgoing.navigator;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for NavigatorSettingsEvent (ID 455).
 * Sends navigator settings (home room) to the client.
 */
public class NavigatorSettingsComposer extends OutgoingMessageComposer {
    private final long homeRoomId;
    
    public NavigatorSettingsComposer(long homeRoomId) {
        this.homeRoomId = homeRoomId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(455); // _events[455] = NavigatorSettingsEvent
        msg.appendUInt(homeRoomId);
        return msg;
    }
}
