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
 */
public class InitTradeHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(InitTradeHandler.class);
    private final Game game;
    
    public InitTradeHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long targetUserId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.InitTradeEvent event = new com.uber.server.event.packet.room.InitTradeEvent(client, message, targetUserId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        targetUserId = event.getTargetUserId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.canTradeInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.RoomUser user = room.getRoomUserByHabbo(habbo.getId());
        if (user == null) {
            return;
        }
        
        // Find user by userId (convert from event's targetUserId)
        com.uber.server.game.rooms.RoomUser user2 = room.getRoomUserByHabbo(targetUserId);
        
        if (user2 == null) {
            return;
        }
        
        room.tryStartTrade(user, user2);
    }
}
