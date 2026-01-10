package com.uber.server.event.packet.handshake;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client sends session parameters (packet ID 206).
 */
public class SendSessionParametersEvent extends PacketReceiveEvent {
    public SendSessionParametersEvent(GameClient client, ClientMessage message) {
        super(client, message, 206);
    }
}
