package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches FAQs (packet ID 419).
 */
public class SearchFaqsEvent extends PacketReceiveEvent {
    private String searchQuery;
    
    public SearchFaqsEvent(GameClient client, ClientMessage message, String searchQuery) {
        super(client, message, 419);
        this.searchQuery = searchQuery;
    }
    
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}
