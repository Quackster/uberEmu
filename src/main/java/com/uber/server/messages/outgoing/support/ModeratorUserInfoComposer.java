package com.uber.server.messages.outgoing.support;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for UserInfoMessageEvent (ID 533).
 * Sends user information for moderation to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class ModeratorUserInfoComposer extends OutgoingMessageComposer {
    private final ServerMessage userInfoMessage;
    
    public ModeratorUserInfoComposer(ServerMessage userInfoMessage) {
        this.userInfoMessage = userInfoMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return userInfoMessage; // Already built with ID 533
    }
}
