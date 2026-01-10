package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests popular room tags (packet ID 382).
 */
public class GetPopularRoomTagsEvent extends PacketReceiveEvent {
    public GetPopularRoomTagsEvent(GameClient client, ClientMessage message) {
        super(client, message, 382);
    }
}
