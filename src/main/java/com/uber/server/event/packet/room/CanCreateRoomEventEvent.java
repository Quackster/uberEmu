package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client checks if they can create a room event (packet ID 345).
 */
public class CanCreateRoomEventEvent extends PacketReceiveEvent {
    public CanCreateRoomEventEvent(GameClient client, ClientMessage message) {
        super(client, message, 345);
    }
}
