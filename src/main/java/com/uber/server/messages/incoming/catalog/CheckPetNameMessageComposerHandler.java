package com.uber.server.messages.incoming.catalog;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for CheckPetNameMessageComposer (ID 42).
 * Processes pet name validation requests from the client.
 */
public class CheckPetNameMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(CheckPetNameMessageComposerHandler.class);
    private final Game game;
    
    public CheckPetNameMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        com.uber.server.game.catalog.Catalog catalog = game.getCatalog();
        if (catalog == null) {
            return;
        }
        
        String petName = message.popFixedString();
        boolean isValid = catalog.checkPetName(petName);
        
        var composer = new com.uber.server.messages.outgoing.catalog.CheckPetNameResponseMessageEventComposer(isValid);
        client.sendMessage(composer.compose());
    }
}
