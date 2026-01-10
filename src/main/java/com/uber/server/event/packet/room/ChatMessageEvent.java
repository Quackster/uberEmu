package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client sends a chat message (packet ID 52).
 */
public class ChatMessageEvent extends PacketReceiveEvent {
    private String message;
    
    public ChatMessageEvent(GameClient client, ClientMessage message, String chatMessage) {
        super(client, message, 52);
        this.message = chatMessage;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
