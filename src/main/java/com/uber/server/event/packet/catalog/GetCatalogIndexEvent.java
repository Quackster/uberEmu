package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests catalog index (packet ID 101).
 */
public class GetCatalogIndexEvent extends PacketReceiveEvent {
    public GetCatalogIndexEvent(GameClient client, ClientMessage message) {
        super(client, message, 101);
    }
}
