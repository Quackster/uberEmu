package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests catalog data part 1 (packet ID 3011).
 */
public class GetCatalogData1Event extends PacketReceiveEvent {
    public GetCatalogData1Event(GameClient client, ClientMessage message) {
        super(client, message, 3011);
    }
}
