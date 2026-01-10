package com.uber.server.handlers.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for removing a favorite room (message ID 20).
 */
public class RemoveFavoriteHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RemoveFavoriteHandler.class);
    private final Game game;
    
    public RemoveFavoriteHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        long roomId = message.popWiredUInt();
        
        // Remove from in-memory list first
        habbo.removeFavoriteRoom(roomId);
        
        // Remove from database
        game.getUserRepository().removeFavorite(habbo.getId(), roomId);
        
        var composer = new com.uber.server.messages.outgoing.navigator.FavouriteChangedEventComposer(roomId, false);
        client.sendMessage(composer.compose());
    }
}
