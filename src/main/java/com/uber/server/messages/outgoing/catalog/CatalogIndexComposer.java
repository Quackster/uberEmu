package com.uber.server.messages.outgoing.catalog;

import com.uber.server.messages.ServerMessage;
import com.uber.server.messages.outgoing.OutgoingMessageComposer;

/**
 * Composer for CatalogIndexMessageEvent (ID 126).
 * Sends catalog index structure to the client.
 */
public class CatalogIndexComposer extends OutgoingMessageComposer {
    private final ServerMessage indexMessage;
    
    /**
     * Creates a composer that wraps an existing catalog index message.
     * Note: Catalog index is complex and built incrementally, so we wrap the pre-built message.
     */
    public CatalogIndexComposer(ServerMessage indexMessage) {
        this.indexMessage = indexMessage;
    }
    
    @Override
    public ServerMessage compose() {
        return indexMessage; // Already built with ID 126
    }
}
