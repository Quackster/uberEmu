package com.uber.server.messages.outgoing.catalog;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for CheckPetNameResponseMessageEvent (ID 36).
 * Response to pet name validation check.
 */
public class ApproveNameComposer extends OutgoingMessageComposer {
    private final int result; // 0 = valid, 2 = invalid
    
    public ApproveNameComposer(boolean isValid) {
        this.result = isValid ? 0 : 2;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(36); // _events[36] = CheckPetNameResponseMessageEvent
        msg.appendInt32(result);
        return msg;
    }
}
