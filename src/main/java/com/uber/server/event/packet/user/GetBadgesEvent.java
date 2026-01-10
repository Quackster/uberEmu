package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests badges (packet ID 157).
 */
public class GetBadgesEvent extends PacketReceiveEvent {
    public GetBadgesEvent(GameClient client, ClientMessage message) {
        super(client, message, 157);
    }
}
