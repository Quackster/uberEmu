package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client completes a trade (packet ID 402).
 */
public class CompleteTradeEvent extends PacketReceiveEvent {
    public CompleteTradeEvent(GameClient client, ClientMessage message) {
        super(client, message, 402);
    }
}
