package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests moodlight data (packet ID 341).
 */
public class GetMoodlightEvent extends PacketReceiveEvent {
    public GetMoodlightEvent(GameClient client, ClientMessage message) {
        super(client, message, 341);
    }
}
