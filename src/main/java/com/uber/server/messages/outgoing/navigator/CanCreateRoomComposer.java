package com.uber.server.messages.outgoing.navigator;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for CanCreateRoomEventEvent (ID 512).
 * Sends room creation permission check result to the client.
 */
public class CanCreateRoomComposer extends OutgoingMessageComposer {
    private final boolean showError;
    private final int roomLimit;
    
    public CanCreateRoomComposer(boolean showError, int roomLimit) {
        this.showError = showError;
        this.roomLimit = roomLimit;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(512); // _events[512] = CanCreateRoomEventEvent
        msg.appendBoolean(showError);
        msg.appendInt32(roomLimit);
        return msg;
    }
}
