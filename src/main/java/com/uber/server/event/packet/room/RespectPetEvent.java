package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client respects a pet (packet ID 3005).
 */
public class RespectPetEvent extends PacketReceiveEvent {
    private long petId;
    
    public RespectPetEvent(GameClient client, ClientMessage message, long petId) {
        super(client, message, 3005);
        this.petId = petId;
    }
    
    public long getPetId() {
        return petId;
    }
    
    public void setPetId(long petId) {
        this.petId = petId;
    }
}
