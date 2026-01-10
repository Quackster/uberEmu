package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for PlaceItemErrorMessageEvent (ID 516).
 * Sends item placement error to the client.
 */
public class PlaceObjectErrorComposer extends OutgoingMessageComposer {
    private final int errorCode;
    
    public PlaceObjectErrorComposer(int errorCode) {
        this.errorCode = errorCode;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(516); // _events[516] = PlaceItemErrorMessageEvent
        msg.appendInt32(errorCode);
        return msg;
    }
}
