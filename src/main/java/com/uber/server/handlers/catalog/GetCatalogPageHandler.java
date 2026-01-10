package com.uber.server.handlers.catalog;

import com.uber.server.game.catalog.Catalog;
import com.uber.server.game.catalog.CatalogItem;
import com.uber.server.game.catalog.CatalogPage;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting a catalog page (message ID 102).
 */
public class GetCatalogPageHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetCatalogPageHandler.class);
    private final Game game;
    
    public GetCatalogPageHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int pageId = message.popWiredInt32();
        
        com.uber.server.event.packet.catalog.GetCatalogPageEvent event = new com.uber.server.event.packet.catalog.GetCatalogPageEvent(
            client, message, pageId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        pageId = event.getPageId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        Catalog catalog = game.getCatalog();
        if (catalog == null) {
            logger.warn("Catalog is not initialized");
            return;
        }
        
        CatalogPage page = catalog.getPage(pageId);
        
        if (page == null || !page.isEnabled() || !page.isVisible() || page.isComingSoon() || page.getMinRank() > habbo.getRank()) {
            return;
        }
        
        if (page.isClubOnly() && !habbo.getSubscriptionManager().hasSubscription("habbo_club")) {
            client.sendNotif("This page is for Uber Club members only!");
            return;
        }
        
        // Use the catalog's serializePage method which returns message ID 127
        ServerMessage response = catalog.serializePage(page);
        client.sendMessage(response);
        
        // Handle special case for recycler layout (send message 507)
        if ("recycler".equals(page.getLayout())) {
            ServerMessage recyclerResponse = new ServerMessage(507);
            recyclerResponse.appendBoolean(true);
            recyclerResponse.appendBoolean(false);
            client.sendMessage(recyclerResponse);
        }
    }
}
