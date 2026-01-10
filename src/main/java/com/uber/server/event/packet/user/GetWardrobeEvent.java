package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests wardrobe (packet ID 375).
 */
public class GetWardrobeEvent extends PacketReceiveEvent {
    public GetWardrobeEvent(GameClient client, ClientMessage message) {
        super(client, message, 375);
    }
}
