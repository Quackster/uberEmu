package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests pet info (packet ID 3001).
 */
public class GetPetInfoEvent extends PacketReceiveEvent {
    private long petId;
    
    public GetPetInfoEvent(GameClient client, ClientMessage message, long petId) {
        super(client, message, 3001);
        this.petId = petId;
    }
    
    public long getPetId() {
        return petId;
    }
    
    public void setPetId(long petId) {
        this.petId = petId;
    }
}
