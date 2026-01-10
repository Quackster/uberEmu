package com.uber.server.messages.incoming.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetClientFaqsMessageComposer (ID 416).
 * Processes help tool initialization requests from the client.
 */
public class GetClientFaqsMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetClientFaqsMessageComposerHandler.class);
    private final Game game;
    
    public GetClientFaqsMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.help.GetClientFaqsEvent event = new com.uber.server.event.packet.help.GetClientFaqsEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // TODO: Replace with FaqClientFaqsMessageEventComposer (ID 518)
        client.sendMessage(game.getHelpTool().serializeFrontpage());
    }
}
