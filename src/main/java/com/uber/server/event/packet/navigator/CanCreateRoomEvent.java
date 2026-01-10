package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client checks if they can create a room (packet ID 387).
 */
public class CanCreateRoomEvent extends PacketReceiveEvent {
    public CanCreateRoomEvent(GameClient client, ClientMessage message) {
        super(client, message, 387);
    }
}
