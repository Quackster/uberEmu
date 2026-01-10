package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator performs a room action (packet ID 460).
 */
public class ModPerformRoomActionEvent extends PacketReceiveEvent {
    private int roomId;
    private int action;
    
    public ModPerformRoomActionEvent(GameClient client, ClientMessage message, int roomId, int action) {
        super(client, message, 460);
        this.roomId = roomId;
        this.action = action;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    
    public int getAction() {
        return action;
    }
    
    public void setAction(int action) {
        this.action = action;
    }
}
