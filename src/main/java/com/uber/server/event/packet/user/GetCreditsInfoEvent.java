package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests credits info (packet ID 8).
 */
public class GetCreditsInfoEvent extends PacketReceiveEvent {
    public GetCreditsInfoEvent(GameClient client, ClientMessage message) {
        super(client, message, 8);
    }
}
