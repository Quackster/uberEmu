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
        Catalog catalog = game.getCatalog();
        if (catalog == null) {
            return;
        }
        
        String petName = message.popFixedString();
        boolean isValid = catalog.checkPetName(petName);
        
        var composer = new com.uber.server.messages.outgoing.catalog.ApproveNameComposer(isValid);
        client.sendMessage(composer.compose());
    }
}
