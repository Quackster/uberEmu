package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client moves their avatar (packet ID 75).
 */
public class MoveAvatarEvent extends PacketReceiveEvent {
    private int x;
    private int y;
    
    public MoveAvatarEvent(GameClient client, ClientMessage message, int x, int y) {
        super(client, message, 75);
        this.x = x;
        this.y = y;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
}
