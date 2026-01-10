package com.uber.server.messages.incoming.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for RequestFurniInventoryComposer (ID 404).
 * Processes furniture inventory requests from the client.
 */
public class RequestFurniInventoryComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestFurniInventoryComposerHandler.class);
    private final Game game;
    
    public RequestFurniInventoryComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.user.RequestFurniInventoryEvent event = new com.uber.server.event.packet.user.RequestFurniInventoryEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        if (habbo.getInventoryComponent() == null) {
            return;
        }
        
        // TODO: Replace with FurniListEventComposer (ID 140)
        var furniInventoryComposer = new com.uber.server.messages.outgoing.users.FurniListComposer(
            habbo.getInventoryComponent().serializeItemInventory());
        client.sendMessage(furniInventoryComposer.compose());
    }
}
