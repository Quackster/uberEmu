package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests client FAQs (packet ID 416).
 */
public class GetClientFaqsEvent extends PacketReceiveEvent {
    public GetClientFaqsEvent(GameClient client, ClientMessage message) {
        super(client, message, 416);
    }
}
