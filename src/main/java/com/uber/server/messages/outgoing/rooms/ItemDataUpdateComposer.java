package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for PostItMessageEvent (ID 48).
 * Sends postit item data to the client.
 */
public class ItemDataUpdateComposer extends OutgoingMessageComposer {
    private final long itemId;
    private final String extraData;
    
    public ItemDataUpdateComposer(long itemId, String extraData) {
        this.itemId = itemId;
        this.extraData = extraData != null ? extraData : "";
    }
    
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(48); // _events[48] = PostItMessageEvent
        msg.appendStringWithBreak(String.valueOf(itemId));
        msg.appendStringWithBreak(extraData);
        return msg;
    }
}
