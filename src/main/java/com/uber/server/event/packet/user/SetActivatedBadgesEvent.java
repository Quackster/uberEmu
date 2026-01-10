package com.uber.server.event.packet.user;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import java.util.List;

/**
 * Event fired when a client sets activated badges (packet ID 158).
 */
public class SetActivatedBadgesEvent extends PacketReceiveEvent {
    private List<String> activatedBadges;
    
    public SetActivatedBadgesEvent(GameClient client, ClientMessage message, List<String> activatedBadges) {
        super(client, message, 158);
        this.activatedBadges = activatedBadges;
    }
    
    public List<String> getActivatedBadges() {
        return activatedBadges;
    }
    
    public void setActivatedBadges(List<String> activatedBadges) {
        this.activatedBadges = activatedBadges;
    }
}
