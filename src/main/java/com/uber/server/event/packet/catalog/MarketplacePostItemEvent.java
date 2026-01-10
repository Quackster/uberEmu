package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client posts an item to marketplace (packet ID 3010).
 */
public class MarketplacePostItemEvent extends PacketReceiveEvent {
    private long itemId;
    private int price;
    
    public MarketplacePostItemEvent(GameClient client, ClientMessage message, long itemId, int price) {
        super(client, message, 3010);
        this.itemId = itemId;
        this.price = price;
    }
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
    
    public int getPrice() {
        return price;
    }
    
    public void setPrice(int price) {
        this.price = price;
    }
}
