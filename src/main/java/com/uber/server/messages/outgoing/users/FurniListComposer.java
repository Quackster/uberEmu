package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for FurniInventoryMessageEvent (ID 140).
 * Sends furni inventory to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class FurniListComposer extends OutgoingMessageComposer {
    private final ServerMessage furniInventoryMessage;
    
    public FurniListComposer(ServerMessage furniInventoryMessage) {
        this.furniInventoryMessage = furniInventoryMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return furniInventoryMessage; // Already built with ID 140
    }
}
