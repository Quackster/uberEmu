package com.uber.server.messages.incoming.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for RedeemVoucherMessageComposer (ID 129).
 * Processes voucher redemption requests from the client.
 */
public class RedeemVoucherMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(RedeemVoucherMessageComposerHandler.class);
    private final Game game;
    
    public RedeemVoucherMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String code = message.popFixedString();
        
        com.uber.server.event.packet.catalog.RedeemVoucherEvent event = new com.uber.server.event.packet.catalog.RedeemVoucherEvent(client, message, code);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        code = event.getVoucherCode();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        if (code == null || code.isEmpty()) {
            // TODO: Replace with VoucherRedeemErrorMessageEventComposer (ID 213)
            ServerMessage error = new ServerMessage(213);
            error.appendRawInt32(1);
            client.sendMessage(error);
            return;
        }
        
        // Check if voucher is valid
        if (!game.getVoucherRepository().isValidCode(code)) {
            // TODO: Replace with VoucherRedeemErrorMessageEventComposer (ID 213)
            ServerMessage error = new ServerMessage(213);
            error.appendRawInt32(1);
            client.sendMessage(error);
            return;
        }
        
        // Get voucher value
        int value = game.getVoucherRepository().getVoucherValue(code);
        
        // Delete voucher
        game.getVoucherRepository().deleteVoucher(code);
        
        if (value > 0) {
            // Add credits to user
            habbo.setCreditsAndUpdate(game.getUserRepository(), habbo.getCredits() + value);
        }
        
        // TODO: Replace with VoucherRedeemOkMessageEventComposer (ID 212)
        ServerMessage success = new ServerMessage(212);
        client.sendMessage(success);
    }
}
