package com.uber.server.event.packet.room;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import java.util.List;

/**
 * Event fired when a client recycles items (packet ID 414).
 */
public class RecycleItemsEvent extends PacketReceiveEvent {
    private List<Long> itemIds;
    
    public RecycleItemsEvent(GameClient client, ClientMessage message, List<Long> itemIds) {
        super(client, message, 414);
        this.itemIds = itemIds;
    }
    
    public List<Long> getItemIds() {
        return itemIds;
    }
    
    public void setItemIds(List<Long> itemIds) {
        this.itemIds = itemIds;
    }
}
