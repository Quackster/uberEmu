package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for GetRoomData1ResponseMessageEvent (ID 297).
 * Response to GetRoomData1 request.
 */
public class FurnitureAliasesComposer extends OutgoingMessageComposer {
    private final int value;
    
    public FurnitureAliasesComposer(int value) {
        this.value = value;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(297); // _events[297] = GetRoomData1ResponseMessageEvent
        msg.appendInt32(value);
        return msg;
    }
}
