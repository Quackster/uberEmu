package com.uber.server.event.packet.help;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a moderator closes a ticket (packet ID 452).
 */
public class ModCloseTicketEvent extends PacketReceiveEvent {
    private int ticketId;
    
    public ModCloseTicketEvent(GameClient client, ClientMessage message, int ticketId) {
        super(client, message, 452);
        this.ticketId = ticketId;
    }
    
    public int getTicketId() {
        return ticketId;
    }
    
    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }
}
