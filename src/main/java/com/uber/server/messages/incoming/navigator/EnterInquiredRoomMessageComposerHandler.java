package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for EnterInquiredRoomMessageComposer (ID 233).
 * Processes room inquiry entry requests from the client.
 * Note: Class name inferred from pattern - should be verified against XML.
 */
public class EnterInquiredRoomMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(EnterInquiredRoomMessageComposerHandler.class);
    private final Game game;
    
    public EnterInquiredRoomMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.navigator.EnterInquiredRoomEvent event = new com.uber.server.event.packet.navigator.EnterInquiredRoomEvent(client, message, roomId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        roomId = event.getRoomId();
        
        // TODO: Implement room inquiry entry functionality
        logger.debug("EnterInquiredRoom called by user {}", 
                    client.getHabbo() != null ? client.getHabbo().getId() : 0);
    }
}
