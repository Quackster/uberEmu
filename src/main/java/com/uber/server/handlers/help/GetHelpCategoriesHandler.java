package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting help categories (message ID 417).
 */
public class GetHelpCategoriesHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetHelpCategoriesHandler.class);
    private final Game game;
    
    public GetHelpCategoriesHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: This handler may be unused - GetFaqCategoriesMessageComposerHandler is registered for ID 417
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 417);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        client.sendMessage(game.getHelpTool().serializeIndex());
    }
}
