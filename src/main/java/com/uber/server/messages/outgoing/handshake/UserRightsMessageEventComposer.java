package com.uber.server.messages.outgoing.handshake;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

import java.util.List;

/**
 * Composer for UserRightsMessageEvent (ID 2).
 * Sent to inform user of their rights/permissions.
 */
public class UserRightsMessageEventComposer extends OutgoingMessageComposer {
    private final List<String> rights;
    
    public UserRightsMessageEventComposer(List<String> rights) {
        this.rights = rights;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(2);
        msg.appendInt32(rights.size());
        
        for (String right : rights) {
            msg.appendStringWithBreak(right);
        }
        
        return msg;
    }
}
