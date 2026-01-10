package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for PetAddedToInventoryMessageEvent (ID 603).
 * Sends pet added to inventory message to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class PetAddedToInventoryComposer extends OutgoingMessageComposer {
    private final ServerMessage petMessage;
    
    public PetAddedToInventoryComposer(ServerMessage petMessage) {
        this.petMessage = petMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return petMessage; // Already built with ID 603
    }
}
