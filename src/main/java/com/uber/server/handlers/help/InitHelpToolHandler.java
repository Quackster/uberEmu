package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for initializing help tool (message ID 416).
 */
public class InitHelpToolHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(InitHelpToolHandler.class);
    private final Game game;
    
    public InitHelpToolHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        // Note: This handler may be unused - GetClientFaqsMessageComposerHandler is registered for ID 416
        com.uber.server.event.packet.GenericPacketEvent event = new com.uber.server.event.packet.GenericPacketEvent(client, message, 416);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        client.sendMessage(game.getHelpTool().serializeFrontpage());
    }
}
