package com.uber.server.messages.outgoing.catalog;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for PurchaseConfirmationMessageEvent (ID 67).
 * Sends purchase confirmation to the client.
 * Note: This message is complex and built incrementally, so we wrap the pre-built message.
 */
public class PurchaseOKComposer extends OutgoingMessageComposer {
    private final ServerMessage confirmationMessage;
    
    public PurchaseOKComposer(ServerMessage confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return confirmationMessage; // Already built with ID 67
    }
}
