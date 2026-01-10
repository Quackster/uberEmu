package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests group badges (packet ID 230).
 */
public class GetGroupBadgesEvent extends PacketReceiveEvent {
    public GetGroupBadgesEvent(GameClient client, ClientMessage message) {
        super(client, message, 230);
    }
}
