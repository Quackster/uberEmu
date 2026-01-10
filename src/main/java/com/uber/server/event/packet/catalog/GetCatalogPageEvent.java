package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests a catalog page (packet ID 102).
 */
public class GetCatalogPageEvent extends PacketReceiveEvent {
    private int pageId;
    
    public GetCatalogPageEvent(GameClient client, ClientMessage message, int pageId) {
        super(client, message, 102);
        this.pageId = pageId;
    }
    
    public int getPageId() {
        return pageId;
    }
    
    public void setPageId(int pageId) {
        this.pageId = pageId;
    }
}
