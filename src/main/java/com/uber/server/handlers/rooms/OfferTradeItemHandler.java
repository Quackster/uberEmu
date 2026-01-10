package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.rooms.Trade;
import com.uber.server.users.inventory.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for offering an item in trade (message ID 72).
 * Ported from Messages/Requests/Rooms.cs OfferTradeItem()
 */
public class OfferTradeItemHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(OfferTradeItemHandler.class);
    private final Game game;
    
    public OfferTradeItemHandler(Game game) {
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
        
        Trade trade = room.getUserTrade(habbo.getId());
        if (trade == null) {
            return;
        }
        
        long itemId = message.popWiredUInt();
        UserItem item = habbo.getInventoryComponent().getItem(itemId);
        
        if (item == null) {
            return;
        }
        
        trade.offerItem(habbo.getId(), item);
    }
}
