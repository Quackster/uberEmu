package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator picks a ticket (packet ID 450).
 */
public class ModPickTicketEvent extends PacketReceiveEvent {
    private int ticketId;
    
    public ModPickTicketEvent(GameClient client, ClientMessage message, int ticketId) {
        super(client, message, 450);
        this.ticketId = ticketId;
    }
    
    public int getTicketId() {
        return ticketId;
    }
    
    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }
}
