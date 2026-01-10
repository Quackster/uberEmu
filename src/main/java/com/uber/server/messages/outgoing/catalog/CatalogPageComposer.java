package com.uber.server.messages.outgoing.catalog;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for CatalogPageMessageEvent (ID 127).
 * Sends catalog page data to the client.
 */
public class CatalogPageComposer extends OutgoingMessageComposer {
    private final ServerMessage pageMessage;
    
    /**
     * Creates a composer that wraps an existing catalog page message.
     * Note: Catalog page is complex and built incrementally, so we wrap the pre-built message.
     */
    public CatalogPageComposer(ServerMessage pageMessage) {
        this.pageMessage = pageMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return pageMessage; // Already built with ID 127
    }
}
