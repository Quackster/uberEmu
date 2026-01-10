package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests FAQ categories (packet ID 417).
 */
public class GetFaqCategoriesEvent extends PacketReceiveEvent {
    public GetFaqCategoriesEvent(GameClient client, ClientMessage message) {
        super(client, message, 417);
    }
}
