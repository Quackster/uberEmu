package com.uber.server.messages.outgoing.catalog;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for GiftWrappingErrorMessageEvent (ID 76).
 * Sent when a gift purchase fails due to invalid recipient.
 */
public class GiftWrappingErrorMessageEventComposer extends OutgoingMessageComposer {
    private final boolean isGift;
    private final String giftUser;
    
    public GiftWrappingErrorMessageEventComposer(boolean isGift, String giftUser) {
        this.isGift = isGift;
        this.giftUser = giftUser;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(76);
        msg.appendBoolean(isGift);
        msg.appendStringWithBreak(giftUser);
        return msg;
    }
}
