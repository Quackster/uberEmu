package com.uber.server.event.packet.global;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client sends a pong (packet ID 196).
 */
public class PongEvent extends PacketReceiveEvent {
    public PongEvent(GameClient client, ClientMessage message) {
        super(client, message, 196);
    }
}
