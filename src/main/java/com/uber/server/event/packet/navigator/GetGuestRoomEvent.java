package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests guest room data (packet ID 385).
 */
public class GetGuestRoomEvent extends PacketReceiveEvent {
    private int roomId;
    
    public GetGuestRoomEvent(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 385);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
