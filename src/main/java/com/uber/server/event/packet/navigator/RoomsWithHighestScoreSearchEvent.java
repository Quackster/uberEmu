package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches for rooms with highest score (packet ID 431).
 */
public class RoomsWithHighestScoreSearchEvent extends PacketReceiveEvent {
    public RoomsWithHighestScoreSearchEvent(GameClient client, ClientMessage message) {
        super(client, message, 431);
    }
}
