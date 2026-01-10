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
 * Ported from Messages/Requests/Navigator.cs AddFavorite()
 */
public class AddFavoriteHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(AddFavoriteHandler.class);
    private final Game game;
    
    public AddFavoriteHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        long roomId = message.popWiredUInt();
        com.uber.server.rooms.RoomData data = game.getRoomManager().generateRoomData(roomId);
        
        if (data == null || habbo.getFavoriteRooms().size() >= 30 || 
            habbo.getFavoriteRooms().contains(roomId) || data.isPublicRoom()) {
            ServerMessage error = new ServerMessage(33);
            error.appendInt32(-9001);
            client.sendMessage(error);
            return;
        }
        
        // Add to database
        if (game.getUserRepository().addFavorite(habbo.getId(), roomId)) {
            habbo.addFavoriteRoom(roomId);
            
            ServerMessage response = new ServerMessage(459);
            response.appendUInt(roomId);
            response.appendBoolean(true);
            client.sendMessage(response);
        }
    }
}
