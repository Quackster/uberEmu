package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for KickUserMessageComposer (ID 95).
 * Processes user kick requests from the client.
 */
public class KickUserMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(KickUserMessageComposerHandler.class);
    private final Game game;
    
    public KickUserMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.KickUserEvent event = new com.uber.server.event.packet.room.KickUserEvent(client, message, userId);
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
        if (room == null || !room.checkRights(client)) {
            return; // Insufficient permissions
        }
        com.uber.server.game.rooms.RoomUser user = room.getRoomUserByHabbo(userId);
        
        if (user == null || user.isBot()) {
            return;
        }
        
        // Can't kick room owner or mods
        GameClient userClient = user.getClient();
        if (userClient != null && userClient.getHabbo() != null) {
            if (room.checkRights(userClient, true) || userClient.getHabbo().hasFuse("fuse_mod")) {
                return; // Can't kick room owner or mods
            }
            
            room.removeUserFromRoom(userClient, true, true);
        }
    }
}
