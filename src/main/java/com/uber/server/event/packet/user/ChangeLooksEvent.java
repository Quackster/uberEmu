package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client changes their appearance (packet ID 44).
 */
public class ChangeLooksEvent extends PacketReceiveEvent {
    private String gender;
    private String figure;
    
    public ChangeLooksEvent(GameClient client, ClientMessage message, String gender, String figure) {
        super(client, message, 44);
        this.gender = gender;
        this.figure = figure;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getFigure() {
        return figure;
    }
    
    public void setFigure(String figure) {
        this.figure = figure;
    }
}
