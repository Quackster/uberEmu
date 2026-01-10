package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client triggers an item (packet IDs 392, 393, 232, 314, 247, 76).
 */
public class TriggerItemEvent extends PacketReceiveEvent {
    private long itemId;
    private int parameter;
    
    public TriggerItemEvent(GameClient client, ClientMessage message, int packetId, long itemId, int parameter) {
        super(client, message, packetId);
        this.itemId = itemId;
        this.parameter = parameter;
    }
    
    public long getItemId() {
        return itemId;
    }
    
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
    
    public int getParameter() {
        return parameter;
    }
    
    public void setParameter(int parameter) {
        this.parameter = parameter;
    }
}
