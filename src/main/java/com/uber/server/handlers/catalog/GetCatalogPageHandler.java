package com.uber.server.handlers.catalog;

import com.uber.server.catalog.Catalog;
import com.uber.server.catalog.CatalogItem;
import com.uber.server.catalog.CatalogPage;
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
 * Ported from Messages/Requests/Catalog.cs GetCatalogPage()
 */
public class GetCatalogPageHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetCatalogPageHandler.class);
    private final Game game;
    
    public GetCatalogPageHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        int pageId = message.popWiredInt32();
        Catalog catalog = game.getCatalog();
        CatalogPage page = catalog.getPage(pageId);
        
        if (page == null) {
            return;
        }
        
        // Check rank access
        if (page.getMinRank() > habbo.getRank()) {
            return;
        }
        
        ServerMessage response = new ServerMessage(102);
        
        // Serialize page info
        response.appendBoolean(page.isVisible());
        response.appendInt32(page.getIconColor());
        response.appendInt32(page.getIconImage());
        response.appendInt32(page.getId());
        response.appendStringWithBreak(page.getCaption());
        response.appendStringWithBreak(page.getLayoutHeadline());
        response.appendStringWithBreak(page.getLayoutTeaser());
        response.appendStringWithBreak(page.getLayoutSpecial());
        response.appendInt32(page.getItems().size());
        
        // Serialize items
        for (CatalogItem item : page.getItems()) {
            if (item.isDeal()) {
                // TODO: Handle deals (multiple items)
                continue;
            }
            
            response.appendUInt(item.getId());
            response.appendStringWithBreak(item.getName());
            response.appendInt32(item.getCreditsCost());
            response.appendInt32(item.getPixelsCost());
            response.appendInt32(1); // Unknown
            response.appendStringWithBreak(item.getBaseItem(game.getItemManager()).getType());
            response.appendInt32(item.getBaseItem(game.getItemManager()).getSpriteId());
            response.appendStringWithBreak("");
            response.appendInt32(item.getAmount());
            response.appendInt32(-1);
        }
        
        client.sendMessage(response);
    }
}
