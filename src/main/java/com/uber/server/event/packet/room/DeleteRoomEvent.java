package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client deletes a room (packet ID 23).
 */
public class DeleteRoomEvent extends PacketReceiveEvent {
    private int roomId;
    
    public DeleteRoomEvent(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 23);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
