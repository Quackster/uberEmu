package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for ShoutMessageEvent (ID 26).
 * Sends a shout message to clients in the room.
 */
public class ShoutMessageComposer extends OutgoingMessageComposer {
    private final int virtualId;
    private final String message;
    private final int emotion;
    
    public ShoutMessageComposer(int virtualId, String message, int emotion) {
        this.virtualId = virtualId;
        this.message = message;
        this.emotion = emotion;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(26); // _events[26] = ShoutMessageEvent
        msg.appendInt32(virtualId);
        msg.appendStringWithBreak(message);
        msg.appendInt32(emotion);
        return msg;
    }
}
