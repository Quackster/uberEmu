package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client claims marketplace credits (packet ID 3016).
 */
public class MarketplaceClaimCreditsEvent extends PacketReceiveEvent {
    public MarketplaceClaimCreditsEvent(GameClient client, ClientMessage message) {
        super(client, message, 3016);
    }
}
