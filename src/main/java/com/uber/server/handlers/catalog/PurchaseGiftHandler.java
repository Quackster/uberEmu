package com.uber.server.handlers.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for purchasing a gift (message ID 472).
 */
public class PurchaseGiftHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseGiftHandler.class);
    private final Game game;
    
    public PurchaseGiftHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int pageId = message.popWiredInt32();
        int itemId = message.popWiredInt32();
        String extraData = message.popFixedString();
        String recipientName = message.popFixedString();
        String giftMessage = message.popFixedString();
        int giftSpriteId = message.popWiredInt32();
        int giftRibbon = message.popWiredInt32();
        int giftBox = message.popWiredInt32();
        
        com.uber.server.event.packet.catalog.PurchaseGiftEvent event = new com.uber.server.event.packet.catalog.PurchaseGiftEvent(client, message, pageId, itemId, extraData, recipientName, giftMessage, giftSpriteId, giftRibbon, giftBox);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        pageId = event.getPageId();
        itemId = event.getItemId();
        extraData = event.getExtraData();
        recipientName = event.getRecipientName();
        giftMessage = event.getGiftMessage();
        giftSpriteId = event.getGiftSpriteId();
        giftRibbon = event.getGiftRibbon();
        giftBox = event.getGiftBox();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        // Filter injection characters
        String giftUser = StringUtil.filterInjectionChars(recipientName, true);
        String giftMsg = StringUtil.filterInjectionChars(giftMessage, true);
        
        game.getCatalog().handlePurchase(client, pageId, itemId, extraData, true, giftUser, giftMsg);
    }
}
