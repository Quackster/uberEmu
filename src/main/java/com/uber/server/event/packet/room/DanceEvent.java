package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client dances (packet ID 93).
 */
public class DanceEvent extends PacketReceiveEvent {
    private int danceId;
    
    public DanceEvent(GameClient client, ClientMessage message, int danceId) {
        super(client, message, 93);
        this.danceId = danceId;
    }
    
    public int getDanceId() {
        return danceId;
    }
    
    public void setDanceId(int danceId) {
        this.danceId = danceId;
    }
}
