package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests pub data (packet ID 388).
 */
public class GetPubEvent extends PacketReceiveEvent {
    public GetPubEvent(GameClient client, ClientMessage message) {
        super(client, message, 388);
    }
}
