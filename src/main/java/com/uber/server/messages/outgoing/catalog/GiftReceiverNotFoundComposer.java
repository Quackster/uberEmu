package com.uber.server.messages.outgoing.catalog;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for GiftWrappingErrorMessageEvent (ID 76).
 * Sends gift wrapping error/status message to the client.
 */
public class GiftReceiverNotFoundComposer extends OutgoingMessageComposer {
    private final boolean success;
    private final String username;
    
    public GiftReceiverNotFoundComposer(boolean success, String username) {
        this.success = success;
        this.username = username;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(76); // _events[76] = GiftWrappingErrorMessageEvent
        msg.appendBoolean(success);
        msg.appendStringWithBreak(username);
        return msg;
    }
}
