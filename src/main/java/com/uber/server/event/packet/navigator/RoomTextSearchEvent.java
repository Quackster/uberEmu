package com.uber.server.event.packet.navigator;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client searches rooms by text (packet ID 437).
 */
public class RoomTextSearchEvent extends PacketReceiveEvent {
    private String searchText;
    
    public RoomTextSearchEvent(GameClient client, ClientMessage message, String searchText) {
        super(client, message, 437);
        this.searchText = searchText;
    }
    
    public String getSearchText() {
        return searchText;
    }
    
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
