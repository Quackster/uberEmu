package com.uber.server.messages.outgoing.support;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for ModerationToolMessageEvent (ID 531).
 * Sends moderation tool data to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class ModeratorInitComposer extends OutgoingMessageComposer {
    private final ServerMessage toolMessage;
    
    public ModeratorInitComposer(ServerMessage toolMessage) {
        this.toolMessage = toolMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return toolMessage; // Already built with ID 531
    }
}
