package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client saves room icon (packet ID 386).
 */
public class SaveRoomIconEvent extends PacketReceiveEvent {
    private int roomId;
    private String iconData;
    
    public SaveRoomIconEvent(GameClient client, ClientMessage message, int roomId, String iconData) {
        super(client, message, 386);
        this.roomId = roomId;
        this.iconData = iconData;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    
    public String getIconData() {
        return iconData;
    }
    
    public void setIconData(String iconData) {
        this.iconData = iconData;
    }
}
