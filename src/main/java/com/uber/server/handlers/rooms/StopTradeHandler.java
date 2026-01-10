package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for stopping a trade (message ID 70, 403).
 */
public class StopTradeHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(StopTradeHandler.class);
    private final Game game;
    
    public StopTradeHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.canTradeInRoom()) {
            return;
        }
        
        room.tryStopTrade(habbo.getId());
    }
}
