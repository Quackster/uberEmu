package com.uber.server.messages.incoming.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for HabboSearchMessageComposer (ID 41).
 * Processes Habbo search requests from the client.
 */
public class HabboSearchMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(HabboSearchMessageComposerHandler.class);
    private final Game game;
    
    public HabboSearchMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getMessenger() == null) {
            return;
        }
        
        String searchQuery = message.popFixedString();
        // TODO: Replace with HabboSearchResultEventComposer (ID 435)
        client.sendMessage(habbo.getMessenger().performSearch(searchQuery));
    }
}
