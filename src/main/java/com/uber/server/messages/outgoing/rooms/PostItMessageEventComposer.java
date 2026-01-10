package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for PostItMessageEvent (ID 48).
 * Sent when opening a post-it note.
 */
public class PostItMessageEventComposer extends OutgoingMessageComposer {
    private final long itemId;
    private final String extraData;
    
    public PostItMessageEventComposer(long itemId, String extraData) {
        this.itemId = itemId;
        this.extraData = extraData;
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(48);
        msg.appendStringWithBreak(String.valueOf(itemId));
        msg.appendStringWithBreak(extraData);
        return msg;
    }
}
