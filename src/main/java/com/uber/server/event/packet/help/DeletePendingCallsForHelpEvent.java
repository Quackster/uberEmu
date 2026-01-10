package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client deletes pending calls for help (packet ID 238).
 */
public class DeletePendingCallsForHelpEvent extends PacketReceiveEvent {
    public DeletePendingCallsForHelpEvent(GameClient client, ClientMessage message) {
        super(client, message, 238);
    }
}
