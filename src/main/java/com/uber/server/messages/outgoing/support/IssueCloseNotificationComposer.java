package com.uber.server.messages.outgoing.support;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for IssueCloseNotificationMessageEvent (ID 540).
 * Sends issue close notification to the client.
 */
public class IssueCloseNotificationComposer extends OutgoingMessageComposer {
    private final int resultCode;
    
    public IssueCloseNotificationComposer(int resultCode) {
        this.resultCode = resultCode;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(540); // _events[540] = IssueCloseNotificationMessageEvent
        msg.appendInt32(resultCode);
        return msg;
    }
}
