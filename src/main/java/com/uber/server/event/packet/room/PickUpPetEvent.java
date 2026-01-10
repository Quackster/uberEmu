package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client picks up a pet (packet ID 3003).
 */
public class PickUpPetEvent extends PacketReceiveEvent {
    private long petId;
    
    public PickUpPetEvent(GameClient client, ClientMessage message, long petId) {
        super(client, message, 3003);
        this.petId = petId;
    }
    
    public long getPetId() {
        return petId;
    }
    
    public void setPetId(long petId) {
        this.petId = petId;
    }
}
