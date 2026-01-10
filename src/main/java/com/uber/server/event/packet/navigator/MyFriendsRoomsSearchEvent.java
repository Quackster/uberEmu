package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches for their friends' rooms (packet ID 432).
 */
public class MyFriendsRoomsSearchEvent extends PacketReceiveEvent {
    public MyFriendsRoomsSearchEvent(GameClient client, ClientMessage message) {
        super(client, message, 432);
    }
}
