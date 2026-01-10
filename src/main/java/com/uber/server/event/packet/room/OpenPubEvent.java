package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client opens a pub (packet ID 2).
 */
public class OpenPubEvent extends PacketReceiveEvent {
    public OpenPubEvent(GameClient client, ClientMessage message) {
        super(client, message, 2);
    }
}
