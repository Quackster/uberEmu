package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for initiating a trade (message ID 71).
 * Ported from Messages/Requests/Rooms.cs InitTrade()
 */
public class InitTradeHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(InitTradeHandler.class);
    private final Game game;
    
    public InitTradeHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.canTradeInRoom()) {
            return;
        }
        
        com.uber.server.rooms.RoomUser user = room.getRoomUserByHabbo(habbo.getId());
        if (user == null) {
            return;
        }
        
        int virtualId = message.popWiredInt32();
        com.uber.server.rooms.RoomUser user2 = room.getRoomUserByVirtualId(virtualId);
        
        if (user2 == null) {
            return;
        }
        
        room.tryStartTrade(user, user2);
    }
}
