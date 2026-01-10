package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client enters the infobus (packet ID 113).
 */
public class EnterInfobusEvent extends PacketReceiveEvent {
    public EnterInfobusEvent(GameClient client, ClientMessage message) {
        super(client, message, 113);
    }
}
