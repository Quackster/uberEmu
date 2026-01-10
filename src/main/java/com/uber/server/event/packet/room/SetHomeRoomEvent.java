package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client sets home room (packet ID 384).
 */
public class SetHomeRoomEvent extends PacketReceiveEvent {
    private int roomId;
    
    public SetHomeRoomEvent(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 384);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
