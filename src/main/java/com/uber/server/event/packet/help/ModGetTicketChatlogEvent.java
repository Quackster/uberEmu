package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator requests ticket chatlog (packet ID 457).
 */
public class ModGetTicketChatlogEvent extends PacketReceiveEvent {
    private int ticketId;
    
    public ModGetTicketChatlogEvent(GameClient client, ClientMessage message, int ticketId) {
        super(client, message, 457);
        this.ticketId = ticketId;
    }
    
    public int getTicketId() {
        return ticketId;
    }
    
    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }
}
