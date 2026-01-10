package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for ActivityPointsMessageEvent (ID 438).
 * Sends activity points (pixels) update to the client.
 */
public class HabboActivityPointNotificationComposer extends OutgoingMessageComposer {
    private final int activityPoints;
    private final int notifAmount;
    
    public HabboActivityPointNotificationComposer(int activityPoints) {
        this(activityPoints, 0);
    }
    
    public HabboActivityPointNotificationComposer(int activityPoints, int notifAmount) {
        this.activityPoints = activityPoints;
        this.notifAmount = notifAmount;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(438); // _events[438] = ActivityPointsMessageEvent
        msg.appendInt32(activityPoints);
        msg.appendInt32(notifAmount);
        return msg;
    }
}
