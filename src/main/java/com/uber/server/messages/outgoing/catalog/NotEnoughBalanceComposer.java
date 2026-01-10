package com.uber.server.messages.outgoing.catalog;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for PurchaseErrorMessageEvent (ID 68).
 * Sends purchase error message (credits/pixels insufficient) to the client.
 */
public class NotEnoughBalanceComposer extends OutgoingMessageComposer {
    private final boolean creditsError;
    private final boolean pixelError;
    
    public NotEnoughBalanceComposer(boolean creditsError, boolean pixelError) {
        this.creditsError = creditsError;
        this.pixelError = pixelError;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(68); // _events[68] = PurchaseErrorMessageEvent
        msg.appendBoolean(creditsError);
        msg.appendBoolean(pixelError);
        return msg;
    }
}
