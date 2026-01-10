package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client stops a trade (packet IDs 70, 403).
 */
public class StopTradeEvent extends PacketReceiveEvent {
    public StopTradeEvent(GameClient client, ClientMessage message, int packetId) {
        super(client, message, packetId);
    }
}
