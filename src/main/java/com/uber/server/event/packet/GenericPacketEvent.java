package com.uber.server.event.packet;

import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Generic event for packets that don't have a specific event class yet.
 * Allows plugins to intercept any packet type.
 */
public class GenericPacketEvent extends PacketReceiveEvent {
    public GenericPacketEvent(GameClient client, ClientMessage message, int packetId) {
        super(client, message, packetId);
    }
}
