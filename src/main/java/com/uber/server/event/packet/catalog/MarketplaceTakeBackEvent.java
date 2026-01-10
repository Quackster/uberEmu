package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client takes back a marketplace item (packet ID 3015).
 */
public class MarketplaceTakeBackEvent extends PacketReceiveEvent {
    private int offerId;
    
    public MarketplaceTakeBackEvent(GameClient client, ClientMessage message, int offerId) {
        super(client, message, 3015);
        this.offerId = offerId;
    }
    
    public int getOfferId() {
        return offerId;
    }
    
    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }
}
