package com.uber.server.event.packet.catalog;

import com.uber.server.event.packet.PacketReceiveEvent;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;

/**
 * Event fired when a client redeems a voucher (packet ID 129).
 */
public class RedeemVoucherEvent extends PacketReceiveEvent {
    private String voucherCode;
    
    public RedeemVoucherEvent(GameClient client, ClientMessage message, String voucherCode) {
        super(client, message, 129);
        this.voucherCode = voucherCode;
    }
    
    public String getVoucherCode() {
        return voucherCode;
    }
    
    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
}
