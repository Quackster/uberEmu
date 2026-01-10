package com.uber.server.messages.outgoing.handshake;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for InfoFeedEnableMessageEvent (ID 517).
 * Sends info feed enable status to the client.
 */
public class InfoFeedEnableComposer extends OutgoingMessageComposer {
    private final boolean enabled;
    
    public InfoFeedEnableComposer(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(517); // _events[517] = InfoFeedEnableMessageEvent
        msg.appendBoolean(enabled);
        return msg;
    }
}
