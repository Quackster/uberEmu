package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for banning a user from a room (message ID 320).
 */
public class BanUserHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(BanUserHandler.class);
    private final Game game;
    
    public BanUserHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.BanUserEvent event = new com.uber.server.event.packet.room.BanUserEvent(client, message, userId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        userId = event.getUserId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return; // Insufficient permissions
        }
        com.uber.server.game.rooms.RoomUser user = room.getRoomUserByHabbo(userId);
        
        if (user == null || user.isBot()) {
            return;
        }
        
        // Can't ban mods
        GameClient userClient = user.getClient();
        if (userClient != null && userClient.getHabbo() != null) {
            if (userClient.getHabbo().hasFuse("fuse_mod")) {
                return;
            }
            
            // Add ban and remove user
            room.addBan(userId);
            room.removeUserFromRoom(userClient, true, true);
        }
    }
}
