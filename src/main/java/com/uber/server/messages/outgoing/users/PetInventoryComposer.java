package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for PetInventoryMessageEvent (ID 600).
 * Sends pet inventory to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class PetInventoryComposer extends OutgoingMessageComposer {
    private final ServerMessage petInventoryMessage;
    
    public PetInventoryComposer(ServerMessage petInventoryMessage) {
        this.petInventoryMessage = petInventoryMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return petInventoryMessage; // Already built with ID 600
    }
}
