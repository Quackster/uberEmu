package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.game.rooms.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for AcceptTradeMessageComposer (ID 69).
 * Processes trade acceptance requests from the client.
 */
public class AcceptTradeMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(AcceptTradeMessageComposerHandler.class);
    private final Game game;
    
    public AcceptTradeMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.room.AcceptTradeEvent event = new com.uber.server.event.packet.room.AcceptTradeEvent(client, message);
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
        
        trade.accept(habbo.getId());
    }
}
