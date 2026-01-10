package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for AchievementsEvent (ID 436).
 * Sends achievement list to the client.
 * Note: This composer is used by AchievementManager.serializeAchievementList() which builds the message body.
 */
public class AchievementsEventComposer extends OutgoingMessageComposer {
    private final ServerMessage message;
    
    /**
     * Creates a composer with a pre-built message.
     * This is used when the message body is built by AchievementManager.
     */
    public AchievementsEventComposer(ServerMessage message) {
        this.message = message;
    }
    
    /**
     * Creates an empty achievements message.
     */
    public AchievementsEventComposer() {
        this.message = new ServerMessage(436);
    }
    
    @Override
    public ServerMessage compose() {
        return message;
    }
}
