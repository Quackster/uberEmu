package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomFloorItemsMessageEvent (ID 32).
 * Sends floor items in the room to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class ObjectsComposer extends OutgoingMessageComposer {
    private final ServerMessage floorItemsMessage;
    
    public ObjectsComposer(ServerMessage floorItemsMessage) {
        this.floorItemsMessage = floorItemsMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return floorItemsMessage; // Already built with ID 32
    }
}
