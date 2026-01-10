package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client opens a postit (packet ID 83).
 */
public class OpenPostitEvent extends PacketReceiveEvent {
    private long itemId;
    
    public OpenPostitEvent(GameClient client, ClientMessage message, long itemId) {
        super(client, message, 83);
        this.itemId = itemId;
    }
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
}
