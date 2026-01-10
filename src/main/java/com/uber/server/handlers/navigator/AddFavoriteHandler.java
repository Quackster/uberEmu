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
 * Handler for adding a favorite room (message ID 19).
 */
public class AddFavoriteHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(AddFavoriteHandler.class);
    private final Game game;
    
    public AddFavoriteHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.navigator.AddFavouriteRoomEvent event = new com.uber.server.event.packet.navigator.AddFavouriteRoomEvent(client, message, roomId);
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
        com.uber.server.game.rooms.RoomData data = game.getRoomManager().generateRoomData(roomIdLong);
        
        if (data == null || habbo.getFavoriteRooms().size() >= 30 || 
            habbo.getFavoriteRooms().contains(roomIdLong) || data.isPublicRoom()) {
            var errorComposer = new com.uber.server.messages.outgoing.global.GenericErrorComposer(-9001);
            client.sendMessage(errorComposer.compose());
            return;
        }
        
        // Add to database
        if (game.getUserRepository().addFavorite(habbo.getId(), roomIdLong)) {
            habbo.addFavoriteRoom(roomIdLong);
            
            var composer = new com.uber.server.messages.outgoing.navigator.FavouriteChangedComposer(roomIdLong, true);
            client.sendMessage(composer.compose());
        }
    }
}
