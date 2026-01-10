package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests official rooms (packet ID 380).
 */
public class GetOfficialRoomsEvent extends PacketReceiveEvent {
    public GetOfficialRoomsEvent(GameClient client, ClientMessage message) {
        super(client, message, 380);
    }
}
