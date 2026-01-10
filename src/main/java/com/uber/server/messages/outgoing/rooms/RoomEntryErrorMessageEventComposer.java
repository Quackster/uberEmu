package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomEntryErrorMessageEvent (ID 18).
 * Sent when a room entry fails or room is being destroyed.
 */
public class RoomEntryErrorMessageEventComposer extends OutgoingMessageComposer {
    
    public RoomEntryErrorMessageEventComposer() {
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(18);
        return msg;
    }
}
