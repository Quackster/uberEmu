package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests user info (packet ID 7).
 */
public class InfoRetrieveEvent extends PacketReceiveEvent {
    public InfoRetrieveEvent(GameClient client, ClientMessage message) {
        super(client, message, 7);
    }
}
