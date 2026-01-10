package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client deletes a postit (packet ID 85).
 */
public class DeletePostitEvent extends PacketReceiveEvent {
    private long itemId;
    
    public DeletePostitEvent(GameClient client, ClientMessage message, long itemId) {
        super(client, message, 85);
        this.itemId = itemId;
    }
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
}
