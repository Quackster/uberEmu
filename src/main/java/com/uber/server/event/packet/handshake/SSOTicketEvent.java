package com.uber.server.event.packet.handshake;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client sends an SSO ticket (packet ID 415).
 */
public class SSOTicketEvent extends PacketReceiveEvent {
    private String ssoTicket;
    
    public SSOTicketEvent(GameClient client, ClientMessage message, String ssoTicket) {
        super(client, message, 415);
        this.ssoTicket = ssoTicket;
    }
    
    /**
     * Gets the SSO ticket.
     * @return SSO ticket string
     */
    public String getSsoTicket() {
        return ssoTicket;
    }
    
    /**
     * Sets the SSO ticket.
     * @param ssoTicket New SSO ticket string
     */
    public void setSsoTicket(String ssoTicket) {
        this.ssoTicket = ssoTicket;
    }
}
