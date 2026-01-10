package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests room data part 1 (packet ID 215).
 */
public class GetRoomData1Event extends PacketReceiveEvent {
    private int roomId;
    
    public GetRoomData1Event(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 215);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
