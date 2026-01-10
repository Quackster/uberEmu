package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomModelNameMessageEvent (ID 69).
 * Sends room model name and room ID to the client.
 */
public class RoomReadyComposer extends OutgoingMessageComposer {
    private final String modelName;
    private final long roomId;
    
    public RoomReadyComposer(String modelName, long roomId) {
        this.modelName = modelName;
        this.roomId = roomId;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(69); // _events[69] = RoomModelNameMessageEvent
        msg.appendStringWithBreak(modelName);
        msg.appendUInt(roomId);
        return msg;
    }
}
