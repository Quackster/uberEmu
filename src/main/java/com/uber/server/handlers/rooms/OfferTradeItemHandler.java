package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.game.rooms.Trade;
import com.uber.server.game.users.inventory.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for offering an item in trade (message ID 72).
 */
public class OfferTradeItemHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(OfferTradeItemHandler.class);
    private final Game game;
    
    public OfferTradeItemHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long itemId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.OfferTradeItemEvent event = new com.uber.server.event.packet.room.OfferTradeItemEvent(client, message, itemId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        itemId = event.getItemId();
        
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
        UserItem item = habbo.getInventoryComponent().getItem(itemId);
        
        if (item == null) {
            return;
        }
        
        trade.offerItem(habbo.getId(), item);
    }
}
