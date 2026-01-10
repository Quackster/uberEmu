package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client opens a present (packet ID 78).
 */
public class OpenPresentEvent extends PacketReceiveEvent {
    private long itemId;
    
    public OpenPresentEvent(GameClient client, ClientMessage message, long itemId) {
        super(client, message, 78);
        this.itemId = itemId;
    }
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
}
