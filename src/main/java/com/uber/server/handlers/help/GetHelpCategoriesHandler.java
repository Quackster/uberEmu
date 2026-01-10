package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting help categories (message ID 417).
 * Ported from Messages/Requests/Help.cs GetHelpCategories()
 */
public class GetHelpCategoriesHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetHelpCategoriesHandler.class);
    private final Game game;
    
    public GetHelpCategoriesHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        client.sendMessage(game.getHelpTool().serializeIndex());
    }
}
