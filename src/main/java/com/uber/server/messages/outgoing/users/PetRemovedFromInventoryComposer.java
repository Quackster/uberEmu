package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for PetRemovedFromInventoryMessageEvent (ID 604).
 * Notifies client that a pet was removed from inventory.
 */
public class PetRemovedFromInventoryComposer extends OutgoingMessageComposer {
    private final long petId;
    
    public PetRemovedFromInventoryComposer(long petId) {
        this.petId = petId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(604); // _events[604] = PetRemovedFromInventoryMessageEvent
        msg.appendUInt(petId);
        return msg;
    }
}
