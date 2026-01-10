package com.uber.server.event.packet.messenger;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import java.util.List;

/**
 * Event fired when a client declines buddy requests (packet ID 38).
 */
public class DeclineBuddyEvent extends PacketReceiveEvent {
    private List<Long> userIds;
    
    public DeclineBuddyEvent(GameClient client, ClientMessage message, List<Long> userIds) {
        super(client, message, 38);
        this.userIds = userIds;
    }
    
    public List<Long> getUserIds() {
        return userIds;
    }
    
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }
}
