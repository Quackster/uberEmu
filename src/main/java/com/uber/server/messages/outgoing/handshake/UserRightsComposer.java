package com.uber.server.messages.outgoing.handshake;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

import java.util.List;

/**
 * Composer for UserRightsMessageEvent (ID 2).
 * Sends user rights/fuses to the client.
 */
public class UserRightsComposer extends OutgoingMessageComposer {
    private final List<String> rights;
    
    public UserRightsComposer(List<String> rights) {
        this.rights = rights != null ? rights : List.of();
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(2); // _events[2] = UserRightsMessageEvent
        msg.appendInt32(rights.size());
        for (String right : rights) {
            msg.appendStringWithBreak(right);
        }
        return msg;
    }
}
