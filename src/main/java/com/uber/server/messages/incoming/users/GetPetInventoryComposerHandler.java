package com.uber.server.messages.incoming.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetPetInventoryComposer (ID 3000).
 * Processes pet inventory requests from the client.
 */
public class GetPetInventoryComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetPetInventoryComposerHandler.class);
    private final Game game;
    
    public GetPetInventoryComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.user.GetPetInventoryEvent event = new com.uber.server.event.packet.user.GetPetInventoryEvent(client, message);
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
        
        var petInventoryComposer = new com.uber.server.messages.outgoing.users.PetInventoryComposer(
            habbo.getInventoryComponent().serializePetInventory());
        client.sendMessage(petInventoryComposer.compose());
    }
}
