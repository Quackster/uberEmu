package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for kicking a bot (message ID 441).
 * Ported from Messages/Requests/Rooms.cs KickBot()
 */
public class KickBotHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(KickBotHandler.class);
    private final Game game;
    
    public KickBotHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        int virtualId = message.popWiredInt32();
        com.uber.server.rooms.RoomUser botUser = room.getRoomUserByVirtualId(virtualId);
        
        if (botUser == null || !botUser.isBot()) {
            return;
        }
        
        // Remove bot from room (kicked = true)
        room.removeBot(virtualId, true);
    }
}
