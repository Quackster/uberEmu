package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client accepts a trade (packet ID 69).
 */
public class AcceptTradeEvent extends PacketReceiveEvent {
    public AcceptTradeEvent(GameClient client, ClientMessage message) {
        super(client, message, 69);
    }
}
