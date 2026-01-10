package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests subscription data (packet ID 26).
 */
public class GetSubscriptionDataEvent extends PacketReceiveEvent {
    public GetSubscriptionDataEvent(GameClient client, ClientMessage message) {
        super(client, message, 26);
    }
}
