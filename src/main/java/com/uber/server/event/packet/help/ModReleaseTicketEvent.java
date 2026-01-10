package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator releases a ticket (packet ID 451).
 */
public class ModReleaseTicketEvent extends PacketReceiveEvent {
    private int ticketId;
    
    public ModReleaseTicketEvent(GameClient client, ClientMessage message, int ticketId) {
        super(client, message, 451);
        this.ticketId = ticketId;
    }
    
    public int getTicketId() {
        return ticketId;
    }
    
    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }
}
