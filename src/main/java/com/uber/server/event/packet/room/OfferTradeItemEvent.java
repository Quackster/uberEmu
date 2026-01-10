package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client offers a trade item (packet ID 72).
 */
public class OfferTradeItemEvent extends PacketReceiveEvent {
    private long itemId;
    
    public OfferTradeItemEvent(GameClient client, ClientMessage message, long itemId) {
        super(client, message, 72);
        this.itemId = itemId;
    }
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
}
