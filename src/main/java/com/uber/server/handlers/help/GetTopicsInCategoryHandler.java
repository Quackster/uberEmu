package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting topics in a category (message ID 420).
 * Ported from Messages/Requests/Help.cs GetTopicsInCategory()
 */
public class GetTopicsInCategoryHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetTopicsInCategoryHandler.class);
    private final Game game;
    
    public GetTopicsInCategoryHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        long categoryId = message.popWiredUInt();
        
        com.uber.server.game.support.HelpCategory category = game.getHelpTool().getCategory(categoryId);
        if (category != null) {
            client.sendMessage(game.getHelpTool().serializeCategory(category));
        }
    }
}
