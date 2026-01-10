package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches their room history (packet ID 436).
 */
public class MyRoomHistorySearchEvent extends PacketReceiveEvent {
    public MyRoomHistorySearchEvent(GameClient client, ClientMessage message) {
        super(client, message, 436);
    }
}
