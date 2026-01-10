package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting topics in a category (message ID 420).
 */
public class GetTopicsInCategoryHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetTopicsInCategoryHandler.class);
    private final Game game;
    
    public GetTopicsInCategoryHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long categoryId = message.popWiredUInt();
        
        // Note: This handler may be unused - GetFaqCategoryMessageComposerHandler is registered for ID 420
        // Use GenericPacketEvent since handler reads data before event (pattern differs from standard)
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 420);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Re-read categoryId after event (event handlers could modify message, but GenericPacketEvent doesn't store fields)
        message.resetPointer();
        categoryId = message.popWiredUInt();
        
        com.uber.server.game.support.HelpCategory category = game.getHelpTool().getCategory(categoryId);
        if (category != null) {
            client.sendMessage(game.getHelpTool().serializeCategory(category));
        }
    }
}
