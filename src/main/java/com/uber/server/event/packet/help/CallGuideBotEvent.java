package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client calls the guide bot (packet ID 440).
 */
public class CallGuideBotEvent extends PacketReceiveEvent {
    public CallGuideBotEvent(GameClient client, ClientMessage message) {
        super(client, message, 440);
    }
}
