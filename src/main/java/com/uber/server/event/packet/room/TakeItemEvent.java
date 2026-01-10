package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client takes an item from a room (packet ID 67).
 */
public class TakeItemEvent extends PacketReceiveEvent {
    private long itemId;
    
    public TakeItemEvent(GameClient client, ClientMessage message, long itemId) {
        super(client, message, 67);
        this.itemId = itemId;
    }
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
}
