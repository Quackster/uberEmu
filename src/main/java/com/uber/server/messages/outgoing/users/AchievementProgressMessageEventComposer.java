package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for AchievementProgressMessageEvent (ID 437).
 * Sends achievement progress/unlock notification to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class AchievementProgressMessageEventComposer extends OutgoingMessageComposer {
    private final ServerMessage achievementMessage;
    
    public AchievementProgressMessageEventComposer(ServerMessage achievementMessage) {
        this.achievementMessage = achievementMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return achievementMessage; // Already built with ID 437
    }
}
