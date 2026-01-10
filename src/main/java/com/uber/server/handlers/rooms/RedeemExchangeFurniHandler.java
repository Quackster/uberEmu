package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.Item;
import com.uber.server.game.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for redeeming exchange furni (message ID 183).
 */
public class RedeemExchangeFurniHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(RedeemExchangeFurniHandler.class);
    private final Game game;
    
    public RedeemExchangeFurniHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long itemId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.RedeemExchangeFurniEvent event = new com.uber.server.event.packet.room.RedeemExchangeFurniEvent(client, message, itemId);
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
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        RoomItem exchangeItem = room.getItem(itemId);
        
        if (exchangeItem == null) {
            return;
        }
        
        Item baseItem = exchangeItem.getBaseItem();
        if (baseItem == null) {
            return;
        }
        
        String itemName = baseItem.getItemName();
        if (itemName == null || (!itemName.startsWith("CF_") && !itemName.startsWith("CFC_"))) {
            return;
        }
        
        // Parse value from item name (e.g., "CF_100" -> 100)
        String[] parts = itemName.split("_");
        if (parts.length < 2) {
            return;
        }
        
        int value;
        try {
            value = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return;
        }
        
        if (value > 0) {
            habbo.setCredits(habbo.getCredits() + value);
            habbo.updateCreditsBalance(game.getUserRepository(), true);
        }
        
        // Remove item from room
        room.removeFurniture(null, itemId);
        
        // Send response (ID 219)
        ServerMessage response = new ServerMessage(219);
        client.sendMessage(response);
    }
}
