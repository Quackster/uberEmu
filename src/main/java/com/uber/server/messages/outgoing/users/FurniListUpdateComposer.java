package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for InventoryRefreshMessageEvent (ID 101).
 * Notifies client to refresh inventory.
 */
public class FurniListUpdateComposer extends OutgoingMessageComposer {
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(101); // _events[101] = InventoryRefreshMessageEvent
        // Empty message body
        return msg;
    }
}
