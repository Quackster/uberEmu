package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client enables an effect (packet ID 373).
 */
public class EnableEffectEvent extends PacketReceiveEvent {
    private int effectId;
    
    public EnableEffectEvent(GameClient client, ClientMessage message, int effectId) {
        super(client, message, 373);
        this.effectId = effectId;
    }
    
    public int getEffectId() {
        return effectId;
    }
    
    public void setEffectId(int effectId) {
        this.effectId = effectId;
    }
}
