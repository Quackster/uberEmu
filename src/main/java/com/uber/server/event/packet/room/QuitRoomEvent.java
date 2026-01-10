package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client quits a room (packet ID 53).
 */
public class QuitRoomEvent extends PacketReceiveEvent {
    public QuitRoomEvent(GameClient client, ClientMessage message) {
        super(client, message, 53);
    }
}
