package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client takes back a trade item (packet ID 405).
 */
public class TakeBackTradeItemEvent extends PacketReceiveEvent {
    private long itemId;
    
    public TakeBackTradeItemEvent(GameClient client, ClientMessage message, long itemId) {
        super(client, message, 405);
        this.itemId = itemId;
    }
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
}
