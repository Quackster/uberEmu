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
 * Handler for DeleteFavouriteRoomMessageComposer (ID 20).
 * Processes favorite room deletion requests from the client.
 */
public class DeleteFavouriteRoomMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeleteFavouriteRoomMessageComposerHandler.class);
    private final Game game;
    
    public DeleteFavouriteRoomMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.navigator.DeleteFavouriteRoomEvent event = new com.uber.server.event.packet.navigator.DeleteFavouriteRoomEvent(client, message, roomId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        roomId = event.getRoomId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        long roomIdLong = roomId;
        
        // Remove from in-memory list first
        habbo.removeFavoriteRoom(roomIdLong);
        
        // Remove from database
        game.getUserRepository().removeFavorite(habbo.getId(), roomIdLong);
        
        var composer = new com.uber.server.messages.outgoing.navigator.FavouriteChangedComposer(roomIdLong, false);
        client.sendMessage(composer.compose());
    }
}
