package com.uber.server.messages.outgoing.support;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for RoomChatlogMessageEvent (ID 535).
 * Sends room chat log to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class RoomChatlogComposer extends OutgoingMessageComposer {
    private final ServerMessage chatlogMessage;
    
    public RoomChatlogComposer(ServerMessage chatlogMessage) {
        this.chatlogMessage = chatlogMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return chatlogMessage; // Already built with ID 535
    }
}
