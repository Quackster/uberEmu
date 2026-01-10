package com.uber.server.messages.outgoing.support;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for UserChatlogMessageEvent (ID 536).
 * Sends user chat log to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class UserChatlogComposer extends OutgoingMessageComposer {
    private final ServerMessage chatlogMessage;
    
    public UserChatlogComposer(ServerMessage chatlogMessage) {
        this.chatlogMessage = chatlogMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return chatlogMessage; // Already built with ID 536
    }
}
