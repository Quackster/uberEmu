package com.uber.server.messages.outgoing.support;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomToolMessageEvent (ID 538).
 * Sends room tool data for moderation to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class ModeratorRoomInfoComposer extends OutgoingMessageComposer {
    private final ServerMessage roomToolMessage;
    
    public ModeratorRoomInfoComposer(ServerMessage roomToolMessage) {
        this.roomToolMessage = roomToolMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return roomToolMessage; // Already built with ID 538
    }
}
