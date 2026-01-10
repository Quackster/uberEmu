package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for GroupBadgesMessageEvent (ID 309).
 * Sends group badges message to the client.
 */
public class HabboGroupBadgesComposer extends OutgoingMessageComposer {
    private final String badgesData;
    
    public HabboGroupBadgesComposer(String badgesData) {
        this.badgesData = badgesData;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(309); // _events[309] = GroupBadgesMessageEvent
        msg.appendStringWithBreak(badgesData);
        return msg;
    }
}
