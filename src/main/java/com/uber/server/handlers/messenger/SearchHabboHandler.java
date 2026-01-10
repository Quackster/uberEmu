package com.uber.server.handlers.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for searching for a Habbo (message ID 41).
 * Ported from Messages/Requests/Messenger.cs SearchHabbo()
 */
public class SearchHabboHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SearchHabboHandler.class);
    private final Game game;
    
    public SearchHabboHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || habbo.getMessenger() == null) {
            return;
        }
        
        String searchQuery = message.popFixedString();
        client.sendMessage(habbo.getMessenger().performSearch(searchQuery));
    }
}
