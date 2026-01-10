package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for checking if user can create a room (message ID 387).
 */
public class CanCreateRoomHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(CanCreateRoomHandler.class);
    private final Game game;
    
    public CanCreateRoomHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.navigator.CanCreateRoomEvent event = new com.uber.server.event.packet.navigator.CanCreateRoomEvent(
            client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // TODO: Implement room limit check when room limit system is added
        // For now, always allow (return false = no error, 99999 = unlimited)
        var composer = new com.uber.server.messages.outgoing.navigator.CanCreateRoomComposer(false, 99999);
        client.sendMessage(composer.compose());
    }
}
