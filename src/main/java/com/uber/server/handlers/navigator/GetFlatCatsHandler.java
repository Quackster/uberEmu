package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.navigator.FlatCat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting flat categories (message ID 151).
 * Ported from Messages/Requests/Navigator.cs GetFlatCats()
 */
public class GetFlatCatsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetFlatCatsHandler.class);
    private final Game game;
    
    public GetFlatCatsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        com.uber.server.navigator.Navigator navigator = game.getNavigator();
        if (navigator == null) {
            return;
        }
        
        client.sendMessage(navigator.serializeFlatCategories());
    }
}
