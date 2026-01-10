package com.uber.server.event.packet.messenger;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests friend list update (packet ID 15).
 */
public class FriendListUpdateEvent extends PacketReceiveEvent {
    public FriendListUpdateEvent(GameClient client, ClientMessage message) {
        super(client, message, 15);
    }
}
