package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator requests room tool (packet ID 459).
 */
public class ModGetRoomToolEvent extends PacketReceiveEvent {
    private int roomId;
    
    public ModGetRoomToolEvent(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 459);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
