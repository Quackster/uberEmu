package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client applies an effect (packet ID 372).
 */
public class ApplyEffectEvent extends PacketReceiveEvent {
    private int effectId;
    
    public ApplyEffectEvent(GameClient client, ClientMessage message, int effectId) {
        super(client, message, 372);
        this.effectId = effectId;
    }
    
    public int getEffectId() {
        return effectId;
    }
    
    public void setEffectId(int effectId) {
        this.effectId = effectId;
    }
}
