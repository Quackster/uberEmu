package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests room edit data (packet ID 400).
 */
public class GetRoomEditDataEvent extends PacketReceiveEvent {
    private int roomId;
    
    public GetRoomEditDataEvent(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 400);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
