package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches for their own rooms (packet ID 434).
 */
public class MyRoomsSearchEvent extends PacketReceiveEvent {
    public MyRoomsSearchEvent(GameClient client, ClientMessage message) {
        super(client, message, 434);
    }
}
