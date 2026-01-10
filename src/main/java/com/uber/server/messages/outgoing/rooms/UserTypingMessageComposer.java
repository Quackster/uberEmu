package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for UserTypingMessageEvent (ID 361).
 * Sends typing status update to clients in the room.
 */
public class UserTypingMessageComposer extends OutgoingMessageComposer {
    private final int virtualId;
    private final boolean isTyping;
    
    public UserTypingMessageComposer(int virtualId, boolean isTyping) {
        this.virtualId = virtualId;
        this.isTyping = isTyping;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(361); // _events[361] = UserTypingMessageEvent
        msg.appendInt32(virtualId);
        msg.appendBoolean(isTyping);
        return msg;
    }
}
