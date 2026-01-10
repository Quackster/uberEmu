package com.uber.server.event.packet.messenger;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches for a Habbo (packet ID 41).
 */
public class HabboSearchEvent extends PacketReceiveEvent {
    private String searchQuery;
    
    public HabboSearchEvent(GameClient client, ClientMessage message, String searchQuery) {
        super(client, message, 41);
        this.searchQuery = searchQuery;
    }
    
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}
