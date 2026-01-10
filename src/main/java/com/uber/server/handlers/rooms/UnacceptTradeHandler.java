package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.game.rooms.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for unaccepting a trade (message ID 68).
 */
public class UnacceptTradeHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(UnacceptTradeHandler.class);
    private final Game game;
    
    public UnacceptTradeHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.room.UnacceptTradeEvent event = new com.uber.server.event.packet.room.UnacceptTradeEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.canTradeInRoom()) {
            return;
        }
        
        Trade trade = room.getUserTrade(habbo.getId());
        if (trade == null) {
            return;
        }
        
        trade.unaccept(habbo.getId());
    }
}
