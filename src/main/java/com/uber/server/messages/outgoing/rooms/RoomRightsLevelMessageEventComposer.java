package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomRightsLevelMessageEvent (ID 42).
 * Sent to notify a user about their room rights level.
 */
public class RoomRightsLevelMessageEventComposer extends OutgoingMessageComposer {
    
    public RoomRightsLevelMessageEventComposer() {
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(42);
        return msg;
    }
}
