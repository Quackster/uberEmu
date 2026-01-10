package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for AvailabilityStatusMessageEvent (ID 290).
 * Sends availability status to the client.
 */
public class AvailabilityStatusComposer extends OutgoingMessageComposer {
    private final boolean isOpen;
    private final boolean isOnDuty;
    
    public AvailabilityStatusComposer(boolean isOpen, boolean isOnDuty) {
        this.isOpen = isOpen;
        this.isOnDuty = isOnDuty;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(290); // _events[290] = AvailabilityStatusMessageEvent
        msg.appendBoolean(isOpen);
        msg.appendBoolean(isOnDuty);
        return msg;
    }
}
