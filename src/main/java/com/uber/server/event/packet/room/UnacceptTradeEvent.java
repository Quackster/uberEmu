package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client unaccepts a trade (packet ID 68).
 */
public class UnacceptTradeEvent extends PacketReceiveEvent {
    public UnacceptTradeEvent(GameClient client, ClientMessage message) {
        super(client, message, 68);
    }
}
