package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client stops typing (packet ID 318).
 */
public class StopTypingEvent extends PacketReceiveEvent {
    public StopTypingEvent(GameClient client, ClientMessage message) {
        super(client, message, 318);
    }
}
