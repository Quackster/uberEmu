package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting favorite rooms (message ID 435).
 * Ported from Messages/Requests/Navigator.cs GetFavoriteRooms()
 */
public class GetFavoriteRoomsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetFavoriteRoomsHandler.class);
    private final Game game;
    
    public GetFavoriteRoomsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        if (game.getNavigator() == null) {
            return;
        }
        
        client.sendMessage(game.getNavigator().serializeFavoriteRooms(client));
    }
}
