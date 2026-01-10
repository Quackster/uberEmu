package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client enters an inquired room (packet ID 233).
 */
public class EnterInquiredRoomEvent extends PacketReceiveEvent {
    private int roomId;
    
    public EnterInquiredRoomEvent(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 233);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
