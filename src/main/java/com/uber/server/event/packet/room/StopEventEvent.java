package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client stops a room event (packet ID 347).
 */
public class StopEventEvent extends PacketReceiveEvent {
    private int eventId;
    
    public StopEventEvent(GameClient client, ClientMessage message, int eventId) {
        super(client, message, 347);
        this.eventId = eventId;
    }
    
    public int getEventId() {
        return eventId;
    }
    
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
}
