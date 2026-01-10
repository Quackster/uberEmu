package com.uber.server.handlers.catalog;

import com.uber.server.game.catalog.Catalog;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for checking pet name (message ID 42).
 */
public class CheckPetNameHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(CheckPetNameHandler.class);
    private final Game game;
    
    public CheckPetNameHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String petName = message.popFixedString();
        
        com.uber.server.event.packet.catalog.CheckPetNameEvent event = new com.uber.server.event.packet.catalog.CheckPetNameEvent(
            client, message, petName);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        petName = event.getPetName();
        
        Catalog catalog = game.getCatalog();
        if (catalog == null) {
            return;
        }
        
        boolean isValid = catalog.checkPetName(petName);
        
        var composer = new com.uber.server.messages.outgoing.catalog.ApproveNameComposer(isValid);
        client.sendMessage(composer.compose());
    }
}
