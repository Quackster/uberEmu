package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for WhisperMessageEvent (ID 25).
 * Sends a whisper message to clients in the room.
 */
public class WhisperMessageComposer extends OutgoingMessageComposer {
    private final int virtualId;
    private final String message;
    private final int emotion;
    
    public WhisperMessageComposer(int virtualId, String message, int emotion) {
        this.virtualId = virtualId;
        this.message = message;
        this.emotion = emotion;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(25); // _events[25] = WhisperMessageEvent
        msg.appendInt32(virtualId);
        msg.appendStringWithBreak(message);
        msg.appendInt32(emotion);
        return msg;
    }
}
