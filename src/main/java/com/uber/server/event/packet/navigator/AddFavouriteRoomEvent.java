package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client adds a favourite room (packet ID 19).
 */
public class AddFavouriteRoomEvent extends PacketReceiveEvent {
    private int roomId;
    
    public AddFavouriteRoomEvent(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 19);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
