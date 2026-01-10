package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests their own marketplace offers (packet ID 3019).
 */
public class MarketplaceGetOwnOffersEvent extends PacketReceiveEvent {
    public MarketplaceGetOwnOffersEvent(GameClient client, ClientMessage message) {
        super(client, message, 3019);
    }
}
