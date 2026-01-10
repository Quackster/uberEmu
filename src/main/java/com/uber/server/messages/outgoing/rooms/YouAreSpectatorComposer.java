package com.uber.server.messages.outgoing.rooms;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for SpectatorModeMessageEvent (ID 254).
 * Sends spectator mode message to the client.
 */
public class YouAreSpectatorComposer extends OutgoingMessageComposer {
    @Override
    public ServerMessage compose() {
        ServerMessage msg = new ServerMessage(254); // _events[254] = SpectatorModeMessageEvent
        // Empty message body
        return msg;
    }
}
