package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomRightsGivenToUserMessageEvent (ID 42).
 * Sent to notify a user they have been given room rights.
 */
public class RoomRightsGivenToUserMessageEventComposer extends OutgoingMessageComposer {
    
    public RoomRightsGivenToUserMessageEventComposer() {
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(42);
        return msg;
    }
}
