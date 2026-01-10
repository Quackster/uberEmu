package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches for latest events (packet ID 439).
 */
public class LatestEventsSearchEvent extends PacketReceiveEvent {
    public LatestEventsSearchEvent(GameClient client, ClientMessage message) {
        super(client, message, 439);
    }
}
