package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client rates a room (packet ID 261).
 */
public class RateFlatEvent extends PacketReceiveEvent {
    private int roomId;
    private int rating;
    
    public RateFlatEvent(GameClient client, ClientMessage message, int roomId, int rating) {
        super(client, message, 261);
        this.roomId = roomId;
        this.rating = rating;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    
    public int getRating() {
        return rating;
    }
    
    public void setRating(int rating) {
        this.rating = rating;
    }
}
