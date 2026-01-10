package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for InventoryRefreshMessageEvent (ID 101).
 * Sent to refresh the user's inventory.
 */
public class InventoryRefreshMessageEventComposer extends OutgoingMessageComposer {
    
    public InventoryRefreshMessageEventComposer() {
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(101);
        return msg;
    }
}
