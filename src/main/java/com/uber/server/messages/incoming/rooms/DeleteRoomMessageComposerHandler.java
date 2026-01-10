package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for DeleteRoomMessageComposer (ID 23).
 * Processes room deletion requests from the client.
 */
public class DeleteRoomMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeleteRoomMessageComposerHandler.class);
    private final Game game;
    
    public DeleteRoomMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        long roomId = message.popWiredUInt();
        com.uber.server.game.rooms.RoomData data = game.getRoomManager().generateRoomData(roomId);
        
        if (data == null || !data.getOwner().toLowerCase().equals(habbo.getUsername().toLowerCase())) {
            return;
        }
        
        // Delete room from database
        if (game.getRoomRepository().deleteRoom(roomId)) {
            // Also delete room items and rights
            game.getRoomItemRepository().deleteRoomItems(roomId);
            game.getRoomRepository().deleteRoomRights(roomId, null);
            
            // Update users with this as home room
            game.getUserRepository().updateHomeRoomForRoom(roomId, 0);
            
            // If room is loaded, kick all users and unload
            com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(roomId);
            if (room != null) {
                // Send kick message to all users
                var kickComposer = new com.uber.server.messages.outgoing.rooms.RoomEntryErrorMessageEventComposer();
                room.sendMessage(kickComposer.compose());
                
                // Remove all users from room
                for (com.uber.server.game.rooms.RoomUser user : room.getUsers().values()) {
                    if (user.isBot()) {
                        continue;
                    }
                    GameClient userClient = user.getClient();
                    if (userClient != null && userClient.getHabbo() != null) {
                        // Set current room to 0
                        userClient.getHabbo().setCurrentRoomId(0);
                    }
                }
                
                // Unload room
                game.getRoomManager().unloadRoom(roomId);
            }
        }
    }
}
