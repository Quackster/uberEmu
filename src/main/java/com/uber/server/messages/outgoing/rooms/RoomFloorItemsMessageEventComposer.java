package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomFloorItemsMessageEvent (ID 32).
 * Wraps a pre-built ServerMessage containing floor items data.
 */
public class RoomFloorItemsMessageEventComposer extends OutgoingMessageComposer {
    private final ServerMessage floorItemsMessage;
    
    public RoomFloorItemsMessageEventComposer(ServerMessage floorItemsMessage) {
        this.floorItemsMessage = floorItemsMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return floorItemsMessage;
    }
}
