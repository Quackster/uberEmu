package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests user flat categories (packet ID 151).
 */
public class GetUserFlatCatsEvent extends PacketReceiveEvent {
    public GetUserFlatCatsEvent(GameClient client, ClientMessage message) {
        super(client, message, 151);
    }
}
