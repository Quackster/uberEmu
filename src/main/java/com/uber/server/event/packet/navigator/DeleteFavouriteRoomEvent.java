package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client deletes a favourite room (packet ID 20).
 */
public class DeleteFavouriteRoomEvent extends PacketReceiveEvent {
    private int roomId;
    
    public DeleteFavouriteRoomEvent(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 20);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
