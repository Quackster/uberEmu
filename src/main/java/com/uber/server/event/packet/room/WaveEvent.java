package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client waves (packet ID 94).
 */
public class WaveEvent extends PacketReceiveEvent {
    public WaveEvent(GameClient client, ClientMessage message) {
        super(client, message, 94);
    }
}
