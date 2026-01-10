package com.uber.server.messages.incoming.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetFaqCategoriesMessageComposer (ID 417).
 * Processes FAQ category requests from the client.
 */
public class GetFaqCategoriesMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetFaqCategoriesMessageComposerHandler.class);
    private final Game game;
    
    public GetFaqCategoriesMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.help.GetFaqCategoriesEvent event = new com.uber.server.event.packet.help.GetFaqCategoriesEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // TODO: Replace with FaqCategoriesMessageEventComposer (ID 519)
        client.sendMessage(game.getHelpTool().serializeIndex());
    }
}
