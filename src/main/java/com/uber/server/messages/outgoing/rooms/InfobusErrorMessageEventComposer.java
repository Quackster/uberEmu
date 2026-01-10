package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for InfobusErrorMessageEvent (ID 81).
 * Sends infobus error/status message to the client.
 */
public class InfobusErrorMessageEventComposer extends OutgoingMessageComposer {
    private final String message;
    
    public InfobusErrorMessageEventComposer(String message) {
        this.message = message != null ? message : "";
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(81); // _events[81] = InfobusErrorMessageEvent
        msg.appendStringWithBreak(message);
        return msg;
    }
}
