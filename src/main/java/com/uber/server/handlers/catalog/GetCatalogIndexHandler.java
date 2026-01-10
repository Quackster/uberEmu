package com.uber.server.handlers.catalog;

import com.uber.server.catalog.Catalog;
import com.uber.server.catalog.CatalogPage;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Handler for getting catalog index (message ID 101).
 * Ported from Messages/Requests/Catalog.cs GetCatalogIndex()
 */
public class GetCatalogIndexHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetCatalogIndexHandler.class);
    private final Game game;
    
    public GetCatalogIndexHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        Catalog catalog = game.getCatalog();
        ServerMessage response = new ServerMessage(101);
        
        // Get root pages (parent_id = -1 or 0)
        int rootCount = 0;
        for (CatalogPage page : catalog.getPages().values()) {
            if (page.getParentId() == -1 || page.getParentId() == 0) {
                if (page.getMinRank() <= habbo.getRank()) {
                    rootCount++;
                }
            }
        }
        
        response.appendInt32(rootCount);
        
        // Serialize root pages
        for (CatalogPage page : catalog.getPages().values()) {
            if (page.getParentId() == -1 || page.getParentId() == 0) {
                if (page.getMinRank() <= habbo.getRank()) {
                    response.appendBoolean(page.isVisible());
                    response.appendInt32(page.getIconColor());
                    response.appendInt32(page.getIconImage());
                    response.appendInt32(page.getId());
                    response.appendStringWithBreak(page.getCaption());
                    response.appendBoolean(page.isComingSoon());
                    response.appendInt32(catalog.getTreeSize(page.getId(), habbo.getRank()));
                }
            }
        }
        
        client.sendMessage(response);
    }
}
