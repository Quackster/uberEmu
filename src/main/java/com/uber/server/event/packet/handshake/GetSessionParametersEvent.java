package com.uber.server.event.packet.handshake;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests session parameters (packet ID 1817).
 */
public class GetSessionParametersEvent extends PacketReceiveEvent {
    public GetSessionParametersEvent(GameClient client, ClientMessage message) {
        super(client, message, 1817);
    }
}
