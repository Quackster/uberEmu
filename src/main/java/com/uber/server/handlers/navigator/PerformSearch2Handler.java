package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for performing a search (variant 2) (message ID 438).
 * Ported from Messages/Requests/Navigator.cs PerformSearch2()
 */
public class PerformSearch2Handler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(PerformSearch2Handler.class);
    private final Game game;
    
    public PerformSearch2Handler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        if (game.getNavigator() == null) {
            return;
        }
        
        int junk = message.popWiredInt32(); // Unused
        String searchQuery = message.popFixedString();
        client.sendMessage(game.getNavigator().serializeSearchResults(searchQuery));
    }
}
