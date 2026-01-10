package com.uber.server.messages.outgoing.catalog;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for PurchaseErrorMessageEvent (ID 68).
 * Sent when a purchase fails due to insufficient credits or pixels.
 */
public class PurchaseErrorMessageEventComposer extends OutgoingMessageComposer {
    private final boolean creditsError;
    private final boolean pixelError;
    
    public PurchaseErrorMessageEventComposer(boolean creditsError, boolean pixelError) {
        this.creditsError = creditsError;
        this.pixelError = pixelError;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(68);
        msg.appendBoolean(creditsError);
        msg.appendBoolean(pixelError);
        return msg;
    }
}
