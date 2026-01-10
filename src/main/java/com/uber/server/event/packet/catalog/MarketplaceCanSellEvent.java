package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client checks if they can sell on marketplace (packet ID 3012).
 */
public class MarketplaceCanSellEvent extends PacketReceiveEvent {
    public MarketplaceCanSellEvent(GameClient client, ClientMessage message) {
        super(client, message, 3012);
    }
}
