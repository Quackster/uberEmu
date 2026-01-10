package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomWallItemsMessageEvent (ID 45).
 * Sends wall items in the room to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class ItemsComposer extends OutgoingMessageComposer {
    private final ServerMessage wallItemsMessage;
    
    public ItemsComposer(ServerMessage wallItemsMessage) {
        this.wallItemsMessage = wallItemsMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return wallItemsMessage; // Already built with ID 45
    }
}
