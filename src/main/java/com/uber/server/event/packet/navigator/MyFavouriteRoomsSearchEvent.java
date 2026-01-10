package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches for their favourite rooms (packet ID 435).
 */
public class MyFavouriteRoomsSearchEvent extends PacketReceiveEvent {
    public MyFavouriteRoomsSearchEvent(GameClient client, ClientMessage message) {
        super(client, message, 435);
    }
}
