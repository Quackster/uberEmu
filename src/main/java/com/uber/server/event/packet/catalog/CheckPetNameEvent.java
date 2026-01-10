package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client checks a pet name (packet ID 42).
 */
public class CheckPetNameEvent extends PacketReceiveEvent {
    private String petName;
    
    public CheckPetNameEvent(GameClient client, ClientMessage message, String petName) {
        super(client, message, 42);
        this.petName = petName;
    }
    
    public String getPetName() {
        return petName;
    }
    
    public void setPetName(String petName) {
        this.petName = petName;
    }
}
