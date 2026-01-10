package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client requests marketplace offers (packet ID 3018).
 */
public class MarketplaceGetOffersEvent extends PacketReceiveEvent {
    private String searchQuery;
    private int minPrice;
    private int maxPrice;
    
    public MarketplaceGetOffersEvent(GameClient client, ClientMessage message, String searchQuery, int minPrice, int maxPrice) {
        super(client, message, 3018);
        this.searchQuery = searchQuery;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
    
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
    
    public int getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(int minPrice) {
        this.minPrice = minPrice;
    }
    
    public int getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }
}
