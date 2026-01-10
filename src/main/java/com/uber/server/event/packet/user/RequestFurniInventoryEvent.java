package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests furniture inventory (packet ID 404).
 */
public class RequestFurniInventoryEvent extends PacketReceiveEvent {
    public RequestFurniInventoryEvent(GameClient client, ClientMessage message) {
        super(client, message, 404);
    }
}
