package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomWallItemsMessageEvent (ID 45).
 * Wraps a pre-built ServerMessage containing wall items data.
 */
public class RoomWallItemsMessageEventComposer extends OutgoingMessageComposer {
    private final ServerMessage wallItemsMessage;
    
    public RoomWallItemsMessageEventComposer(ServerMessage wallItemsMessage) {
        this.wallItemsMessage = wallItemsMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return wallItemsMessage;
    }
}
