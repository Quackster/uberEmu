package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client checks if they can gift (packet ID 3030).
 */
public class CanGiftEvent extends PacketReceiveEvent {
    public CanGiftEvent(GameClient client, ClientMessage message) {
        super(client, message, 3030);
    }
}
