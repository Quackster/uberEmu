package com.uber.server.messages.outgoing.support;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for ModerationActionResultMessageEvent (ID 540).
 * Sends moderation action result to the client.
 */
public class ModerationActionResultMessageEventComposer extends OutgoingMessageComposer {
    private final int resultCode;
    
    public ModerationActionResultMessageEventComposer(int resultCode) {
        this.resultCode = resultCode;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(540); // _events[540] = ModerationActionResultMessageEvent
        msg.appendInt32(resultCode);
        return msg;
    }
}
