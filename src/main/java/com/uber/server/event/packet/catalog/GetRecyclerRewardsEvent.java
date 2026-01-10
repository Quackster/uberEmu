package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests recycler rewards (packet ID 412).
 */
public class GetRecyclerRewardsEvent extends PacketReceiveEvent {
    public GetRecyclerRewardsEvent(GameClient client, ClientMessage message) {
        super(client, message, 412);
    }
}
