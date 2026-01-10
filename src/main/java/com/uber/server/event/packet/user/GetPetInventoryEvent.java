package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests pet inventory (packet ID 3000).
 */
public class GetPetInventoryEvent extends PacketReceiveEvent {
    public GetPetInventoryEvent(GameClient client, ClientMessage message) {
        super(client, message, 3000);
    }
}
