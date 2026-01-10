package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client starts typing (packet ID 361).
 */
public class StartTypingEvent extends PacketReceiveEvent {
    public StartTypingEvent(GameClient client, ClientMessage message) {
        super(client, message, 361);
    }
}
