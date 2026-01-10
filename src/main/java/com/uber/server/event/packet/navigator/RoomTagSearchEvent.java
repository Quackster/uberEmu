package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches rooms by tag (packet ID 438).
 */
public class RoomTagSearchEvent extends PacketReceiveEvent {
    private String tag;
    
    public RoomTagSearchEvent(GameClient client, ClientMessage message, String tag) {
        super(client, message, 438);
        this.tag = tag;
    }
    
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
}
