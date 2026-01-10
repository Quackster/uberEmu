package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for AchievementUnlockedMessageEvent (ID 437).
 * Sends achievement unlocked notification to the client.
 */
public class HabboAchievementNotificationComposer extends OutgoingMessageComposer {
    private final ServerMessage message;
    
    /**
     * Creates a composer with a pre-built message.
     * This is used when the message body is built by AchievementManager.
     */
    public HabboAchievementNotificationComposer(ServerMessage message) {
        this.message = message;
    }
    
    @Override
    public ServerMessage compose() {
        return message;
    }
}
