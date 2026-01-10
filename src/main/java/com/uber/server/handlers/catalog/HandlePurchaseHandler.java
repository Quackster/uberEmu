package com.uber.server.handlers.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for catalog purchase (message ID 100).
 */
public class HandlePurchaseHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(HandlePurchaseHandler.class);
    private final Game game;
    
    public HandlePurchaseHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: This handler uses a simplified purchase format (legacy)
        // The proper handler is HandlePurchaseMessageComposerHandler which handles the full format
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 100);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        if (client.getHabbo() == null) {
            return;
        }
        
        int pageId = message.popWiredInt32();
        long itemId = message.popWiredUInt();
        String extraData = message.popFixedString();
        
        // Call catalog handlePurchase (not a gift purchase)
        game.getCatalog().handlePurchase(client, pageId, itemId, extraData, false, "", "");
    }
}
