package com.uber.server.messages.outgoing.users;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for CreditsMessageEvent (ID 6).
 * Sends credits balance update to the client.
 */
public class CreditBalanceComposer extends OutgoingMessageComposer {
    private final int credits;
    
    public CreditBalanceComposer(int credits) {
        this.credits = credits;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(6); // _events[6] = CreditsMessageEvent
        msg.appendStringWithBreak(credits + ".0");
        return msg;
    }
}
