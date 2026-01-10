package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client applies a room effect (packet ID 66).
 */
public class ApplyRoomEffectEvent extends PacketReceiveEvent {
    private int effectId;
    
    public ApplyRoomEffectEvent(GameClient client, ClientMessage message, int effectId) {
        super(client, message, 66);
        this.effectId = effectId;
    }
    
    public int getEffectId() {
        return effectId;
    }
    
    public void setEffectId(int effectId) {
        this.effectId = effectId;
    }
}
