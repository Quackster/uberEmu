package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests room data part 3 (packet ID 126).
 */
public class GetRoomData3Event extends PacketReceiveEvent {
    private int roomId;
    
    public GetRoomData3Event(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 126);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
