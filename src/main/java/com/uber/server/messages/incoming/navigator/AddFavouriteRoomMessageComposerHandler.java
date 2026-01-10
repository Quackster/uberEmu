package com.uber.server.messages.incoming.navigator;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for AddFavouriteRoomMessageComposer (ID 19).
 * Processes favorite room addition requests from the client.
 */
public class AddFavouriteRoomMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(AddFavouriteRoomMessageComposerHandler.class);
    private final Game game;
    
    public AddFavouriteRoomMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        long roomId = message.popWiredUInt();
        var data = game.getRoomManager().generateRoomData(roomId);
        
        if (data == null || habbo.getFavoriteRooms().size() >= 30 || 
            habbo.getFavoriteRooms().contains(roomId) || data.isPublicRoom()) {
            var errorComposer = new com.uber.server.messages.outgoing.global.GenericErrorComposer(-9001);
            client.sendMessage(errorComposer.compose());
            return;
        }
        
        // Add to database
        if (game.getUserRepository().addFavorite(habbo.getId(), roomId)) {
            habbo.addFavoriteRoom(roomId);
            
            var composer = new com.uber.server.messages.outgoing.navigator.FavouriteChangedComposer(roomId, true);
            client.sendMessage(composer.compose());
        }
    }
}
