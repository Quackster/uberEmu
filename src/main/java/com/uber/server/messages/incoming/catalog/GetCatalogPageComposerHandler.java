package com.uber.server.messages.incoming.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetCatalogPageComposer (ID 102).
 * Processes catalog page requests from the client.
 */
public class GetCatalogPageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetCatalogPageComposerHandler.class);
    private final Game game;
    
    public GetCatalogPageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int pageId = message.popWiredInt32();
        
        com.uber.server.event.packet.catalog.GetCatalogPageEvent event = new com.uber.server.event.packet.catalog.GetCatalogPageEvent(client, message, pageId);
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
        var catalog = game.getCatalog();
        if (catalog == null) {
            logger.warn("Catalog is not initialized");
            return;
        }
        
        var page = catalog.getPage(pageId);
        
        if (page == null || !page.isEnabled() || !page.isVisible() || page.isComingSoon() || page.getMinRank() > habbo.getRank()) {
            return;
        }
        
        if (page.isClubOnly() && !habbo.getSubscriptionManager().hasSubscription("habbo_club")) {
            client.sendNotif("This page is for Uber Club members only!");
            return;
        }
        
        // Use the catalog's serializePage method which returns message ID 127
        // TODO: Replace with CatalogPageMessageEventComposer
        var response = catalog.serializePage(page);
        client.sendMessage(response);
        
        // Handle special case for recycler layout (send message 507)
        if ("recycler".equals(page.getLayout())) {
            // TODO: Replace with RecyclerStatusMessageEventComposer
            var recyclerResponse = new com.uber.server.messages.ServerMessage(507);
            recyclerResponse.appendBoolean(true);
            recyclerResponse.appendBoolean(false);
            client.sendMessage(recyclerResponse);
        }
    }
}
