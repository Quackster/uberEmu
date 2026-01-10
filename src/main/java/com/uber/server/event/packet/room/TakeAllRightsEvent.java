package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client takes all room rights (packet ID 155).
 */
public class TakeAllRightsEvent extends PacketReceiveEvent {
    public TakeAllRightsEvent(GameClient client, ClientMessage message) {
        super(client, message, 155);
    }
}
