package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests achievements (packet ID 370).
 */
public class GetAchievementsEvent extends PacketReceiveEvent {
    public GetAchievementsEvent(GameClient client, ClientMessage message) {
        super(client, message, 370);
    }
}
