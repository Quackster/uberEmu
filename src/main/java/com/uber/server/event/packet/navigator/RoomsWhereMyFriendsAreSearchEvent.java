package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches for rooms where their friends are (packet ID 433).
 */
public class RoomsWhereMyFriendsAreSearchEvent extends PacketReceiveEvent {
    public RoomsWhereMyFriendsAreSearchEvent(GameClient client, ClientMessage message) {
        super(client, message, 433);
    }
}
