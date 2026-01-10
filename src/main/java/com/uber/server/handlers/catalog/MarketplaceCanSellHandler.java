package com.uber.server.handlers.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for checking if user can sell on marketplace (message ID 3012).
 * Ported from Messages/Requests/Catalog.cs MarketplaceCanSell()
 */
public class MarketplaceCanSellHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(MarketplaceCanSellHandler.class);
    private final Game game;
    
    public MarketplaceCanSellHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        ServerMessage response = new ServerMessage(611);
        response.appendBoolean(true); // Can sell
        response.appendInt32(99999); // Max price
        client.sendMessage(response);
    }
}
