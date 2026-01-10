package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for AvailabilityStatusMessageEvent (ID 3).
 * Sent to set user availability status.
 */
public class AvailabilityStatusMessageEventComposer extends OutgoingMessageComposer {
    private final boolean isOpen;
    private final boolean isLookingForFriends;
    
    public AvailabilityStatusMessageEventComposer(boolean isOpen, boolean isLookingForFriends) {
        this.isOpen = isOpen;
        this.isLookingForFriends = isLookingForFriends;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(3);
        msg.appendBoolean(isOpen);
        msg.appendBoolean(isLookingForFriends);
        return msg;
    }
}
