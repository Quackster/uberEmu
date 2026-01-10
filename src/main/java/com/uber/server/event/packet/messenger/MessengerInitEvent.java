package com.uber.server.event.packet.messenger;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client initializes messenger (packet ID 12).
 */
public class MessengerInitEvent extends PacketReceiveEvent {
    public MessengerInitEvent(GameClient client, ClientMessage message) {
        super(client, message, 12);
    }
}
