package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client purchases from marketplace (packet ID 3014).
 */
public class MarketplacePurchaseEvent extends PacketReceiveEvent {
    private int offerId;
    
    public MarketplacePurchaseEvent(GameClient client, ClientMessage message, int offerId) {
        super(client, message, 3014);
        this.offerId = offerId;
    }
    
    public int getOfferId() {
        return offerId;
    }
    
    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }
}
