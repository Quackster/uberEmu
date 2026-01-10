package com.uber.server.event.packet.messenger;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import java.util.List;

/**
 * Event fired when a client sends a room invite (packet ID 34).
 */
public class SendRoomInviteEvent extends PacketReceiveEvent {
    private List<Long> userIds;
    private String message;
    
    public SendRoomInviteEvent(GameClient client, ClientMessage message, List<Long> userIds, String messageText) {
        super(client, message, 34);
        this.userIds = userIds;
        this.message = messageText;
    }
    
    public List<Long> getUserIds() {
        return userIds;
    }
    
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
