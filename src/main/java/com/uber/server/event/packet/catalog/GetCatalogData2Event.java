package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests catalog data part 2 (packet ID 473).
 */
public class GetCatalogData2Event extends PacketReceiveEvent {
    public GetCatalogData2Event(GameClient client, ClientMessage message) {
        super(client, message, 473);
    }
}
