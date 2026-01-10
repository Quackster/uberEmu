package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests room data part 2 (packet ID 390).
 */
public class GetRoomData2Event extends PacketReceiveEvent {
    private int roomId;
    
    public GetRoomData2Event(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 390);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
