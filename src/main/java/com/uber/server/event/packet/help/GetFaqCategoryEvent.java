package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests FAQ category (packet ID 420).
 */
public class GetFaqCategoryEvent extends PacketReceiveEvent {
    private int categoryId;
    
    public GetFaqCategoryEvent(GameClient client, ClientMessage message, int categoryId) {
        super(client, message, 420);
        this.categoryId = categoryId;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}
