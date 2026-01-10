package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for ActivityPointsMessageEvent (ID 438).
 * Sends activity points (pixels) update to the client.
 */
public class ActivityPointsMessageEventComposer extends OutgoingMessageComposer {
    private final int activityPoints;
    private final int notifAmount;
    
    public ActivityPointsMessageEventComposer(int activityPoints) {
        this(activityPoints, 0);
    }
    
    public ActivityPointsMessageEventComposer(int activityPoints, int notifAmount) {
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
