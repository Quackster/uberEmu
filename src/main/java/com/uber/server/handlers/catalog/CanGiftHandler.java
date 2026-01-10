package com.uber.server.handlers.catalog;

import com.uber.server.game.catalog.Catalog;
import com.uber.server.game.catalog.CatalogItem;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for checking if an item can be gifted (message ID 3030).
 */
public class CanGiftHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(CanGiftHandler.class);
    private final Game game;
    
    public CanGiftHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.catalog.CanGiftEvent event = new com.uber.server.event.packet.catalog.CanGiftEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Catalog catalog = game.getCatalog();
        if (catalog == null) {
            return;
        }
        
        long itemId = message.popWiredUInt();
        CatalogItem item = catalog.findItem(itemId);
        
        if (item == null) {
            return;
        }
        
        com.uber.server.game.items.Item baseItem = item.getBaseItem(this.game.getItemManager());
        if (baseItem == null) {
            return;
        }
        
        ServerMessage response = new ServerMessage(622);
        response.appendUInt(item.getId());
        response.appendBoolean(baseItem.allowGift());
        client.sendMessage(response);
    }
}
