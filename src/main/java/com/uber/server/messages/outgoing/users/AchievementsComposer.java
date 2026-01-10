package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for AchievementsListMessageEvent (ID 436).
 * Sends achievements list to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class AchievementsComposer extends OutgoingMessageComposer {
    private final ServerMessage achievementsMessage;
    
    public AchievementsComposer(ServerMessage achievementsMessage) {
        this.achievementsMessage = achievementsMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return achievementsMessage; // Already built with ID 436
    }
}
