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
        message.resetPointer();
        
        int categoryId = message.popWiredInt32();
        
        com.uber.server.event.packet.help.GetFaqCategoryEvent event = new com.uber.server.event.packet.help.GetFaqCategoryEvent(client, message, categoryId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        categoryId = event.getCategoryId();
        
        long categoryIdLong = categoryId;
        
        var category = game.getHelpTool().getCategory(categoryIdLong);
        if (category != null) {
            // TODO: Replace with FaqCategoryMessageEventComposer (ID 522)
            client.sendMessage(game.getHelpTool().serializeCategory(category));
        }
    }
}
