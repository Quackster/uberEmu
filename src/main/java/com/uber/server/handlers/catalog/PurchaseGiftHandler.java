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
 * Ported from Messages/Requests/Catalog.cs PurchaseGift()
 */
public class PurchaseGiftHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseGiftHandler.class);
    private final Game game;
    
    public PurchaseGiftHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        int pageId = message.popWiredInt32();
        long itemId = message.popWiredUInt();
        String extraData = message.popFixedString();
        String giftUser = StringUtil.filterInjectionChars(message.popFixedString(), true);
        String giftMessage = StringUtil.filterInjectionChars(message.popFixedString(), true);
        
        game.getCatalog().handlePurchase(client, pageId, itemId, extraData, true, giftUser, giftMessage);
    }
}
