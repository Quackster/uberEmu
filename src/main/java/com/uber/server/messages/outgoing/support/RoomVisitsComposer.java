package com.uber.server.messages.outgoing.support;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomVisitsMessageEvent (ID 537).
 * Sends room visits for a user to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class RoomVisitsComposer extends OutgoingMessageComposer {
    private final ServerMessage visitsMessage;
    
    public RoomVisitsComposer(ServerMessage visitsMessage) {
        this.visitsMessage = visitsMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return visitsMessage; // Already built with ID 537
    }
}
