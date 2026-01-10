package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for FurniRemovedFromInventoryMessageEvent (ID 99).
 * Notifies client that an item was removed from inventory.
 */
public class FurniListRemoveComposer extends OutgoingMessageComposer {
    private final long itemId;
    
    public FurniListRemoveComposer(long itemId) {
        this.itemId = itemId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(99); // _events[99] = FurniRemovedFromInventoryMessageEvent
        msg.appendUInt(itemId);
        return msg;
    }
}
