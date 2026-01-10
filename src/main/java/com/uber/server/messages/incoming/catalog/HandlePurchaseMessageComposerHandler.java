package com.uber.server.messages.incoming.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for HandlePurchaseMessageComposer (ID 100).
 * Processes catalog purchase requests from the client.
 */
public class HandlePurchaseMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(HandlePurchaseMessageComposerHandler.class);
    private final Game game;
    
    public HandlePurchaseMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int pageId = message.popWiredInt32();
        java.util.List<Integer> itemIds = new java.util.ArrayList<>();
        int itemCount = message.popWiredInt32();
        for (int i = 0; i < itemCount; i++) {
            itemIds.add(message.popWiredInt32());
        }
        String extraData = message.popFixedString();
        String recipientName = message.popFixedString();
        String giftMessage = message.popFixedString();
        
        com.uber.server.event.packet.catalog.HandlePurchaseEvent event = new com.uber.server.event.packet.catalog.HandlePurchaseEvent(client, message, pageId, itemIds, extraData, recipientName, giftMessage);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        pageId = event.getPageId();
        itemIds = event.getItemIds();
        extraData = event.getExtraData();
        recipientName = event.getRecipientName();
        giftMessage = event.getGiftMessage();
        
        if (client.getHabbo() == null) {
            return;
        }
        
        // Process first item (handler logic may need adjustment for multiple items)
        if (!itemIds.isEmpty()) {
            long itemId = itemIds.get(0);
            boolean isGift = (recipientName != null && !recipientName.isEmpty());
            // Call catalog handlePurchase
            game.getCatalog().handlePurchase(client, pageId, itemId, extraData, isGift, recipientName != null ? recipientName : "", giftMessage != null ? giftMessage : "");
        }
    }
}
