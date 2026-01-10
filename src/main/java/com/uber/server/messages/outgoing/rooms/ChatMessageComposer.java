package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for ChatMessageEvent (ID 24).
 * Sends a chat message to clients in the room.
 */
public class ChatMessageComposer extends OutgoingMessageComposer {
    private final int virtualId;
    private final String message;
    private final int emotion;
    
    public ChatMessageComposer(int virtualId, String message, int emotion) {
        this.virtualId = virtualId;
        this.message = message;
        this.emotion = emotion;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(24); // _events[24] = ChatMessageEvent
        msg.appendInt32(virtualId);
        msg.appendStringWithBreak(message);
        msg.appendInt32(emotion);
        return msg;
    }
}
