package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for SubscriptionDataMessageEvent (ID 7).
 * Sends subscription data to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class ScrSendUserInfoComposer extends OutgoingMessageComposer {
    private final ServerMessage subscriptionMessage;
    
    public ScrSendUserInfoComposer(ServerMessage subscriptionMessage) {
        this.subscriptionMessage = subscriptionMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return subscriptionMessage; // Already built with ID 7
    }
}
