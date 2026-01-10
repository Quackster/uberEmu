package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomEventEvent (ID 370).
 * Wraps RoomEvent serialization or creates empty event message.
 */
public class RoomEventComposer extends OutgoingMessageComposer {
    private final ServerMessage message;
    
    /**
     * Creates a composer with a pre-built message from RoomEvent.serialize().
     */
    public RoomEventComposer(ServerMessage message) {
        this.message = message;
    }
    
    /**
     * Creates an empty room event message (no event).
     */
    public RoomEventComposer() {
        this.message = new ServerMessage(370);
        message.appendStringWithBreak("-1");
    }
    
    @Override
    public ServerMessage compose() {
        return message;
    }
}
