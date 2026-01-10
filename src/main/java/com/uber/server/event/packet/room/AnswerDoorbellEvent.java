package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client answers a doorbell (packet ID 98).
 */
public class AnswerDoorbellEvent extends PacketReceiveEvent {
    private String username;
    private byte answer; // 65 = 'A' (accept), anything else = decline
    
    public AnswerDoorbellEvent(GameClient client, ClientMessage message, String username, byte answer) {
        super(client, message, 98);
        this.username = username;
        this.answer = answer;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public byte getAnswer() {
        return answer;
    }
    
    public void setAnswer(byte answer) {
        this.answer = answer;
    }
    
    public boolean isAccept() {
        return answer == 65; // 65 = 'A' (accept)
    }
}
