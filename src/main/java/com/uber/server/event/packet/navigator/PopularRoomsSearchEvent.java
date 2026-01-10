package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches for popular rooms (packet ID 430).
 */
public class PopularRoomsSearchEvent extends PacketReceiveEvent {
    public PopularRoomsSearchEvent(GameClient client, ClientMessage message) {
        super(client, message, 430);
    }
}
