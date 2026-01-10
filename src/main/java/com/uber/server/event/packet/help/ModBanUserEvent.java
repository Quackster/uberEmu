package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator bans a user (packet ID 464).
 */
public class ModBanUserEvent extends PacketReceiveEvent {
    private long userId;
    private String message;
    private int banHours;
    
    public ModBanUserEvent(GameClient client, ClientMessage message, long userId, String banMessage, int banHours) {
        super(client, message, 464);
        this.userId = userId;
        this.message = banMessage;
        this.banHours = banHours;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getBanHours() {
        return banHours;
    }
    
    public void setBanHours(int banHours) {
        this.banHours = banHours;
    }
}
