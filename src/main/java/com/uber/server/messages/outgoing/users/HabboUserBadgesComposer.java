package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;
import com.uber.server.game.users.badges.Badge;

import java.util.List;

/**
 * Composer for BadgesEvent (ID 228).
 * Sends equipped badges update to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class HabboUserBadgesComposer extends OutgoingMessageComposer {
    private final ServerMessage badgesMessage;
    
    public HabboUserBadgesComposer(ServerMessage badgesMessage) {
        this.badgesMessage = badgesMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return badgesMessage; // Already built with ID 228
    }
}
