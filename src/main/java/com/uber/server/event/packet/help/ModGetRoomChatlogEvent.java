package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator requests room chatlog (packet ID 456).
 */
public class ModGetRoomChatlogEvent extends PacketReceiveEvent {
    private int roomId;
    
    public ModGetRoomChatlogEvent(GameClient client, ClientMessage message, int roomId) {
        super(client, message, 456);
        this.roomId = roomId;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
