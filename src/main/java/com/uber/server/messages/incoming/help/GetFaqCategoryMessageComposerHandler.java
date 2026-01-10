package com.uber.server.messages.incoming.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetFaqCategoryMessageComposer (ID 420).
 * Processes FAQ category topic requests from the client.
 */
public class GetFaqCategoryMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetFaqCategoryMessageComposerHandler.class);
    private final Game game;
    
    public GetFaqCategoryMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        long categoryId = message.popWiredUInt();
        
        var category = game.getHelpTool().getCategory(categoryId);
        if (category != null) {
            // TODO: Replace with FaqCategoryMessageEventComposer (ID 522)
            client.sendMessage(game.getHelpTool().serializeCategory(category));
        }
    }
}
