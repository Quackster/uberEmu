package com.uber.server.handlers.rooms;

import com.uber.server.event.packet.room.OpenPresentEvent;
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

import java.util.Map;

/**
 * Handler for opening a present (message ID 78).
 */
public class OpenPresentHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(OpenPresentHandler.class);
    private final Game game;
    
    public OpenPresentHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long itemId = message.popWiredUInt();
        
        OpenPresentEvent event = new OpenPresentEvent(client, message, itemId);
        Game.getInstance().getEventManager().callEvent(event);
        
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
        RoomItem presentItem = room.getItem(itemId);
        
        if (presentItem == null) {
            return;
        }
        
        // Get present data from database
        Map<String, Object> presentData = game.getInventoryRepository().getUserPresent(itemId);
        if (presentData == null) {
            return;
        }
        
        long baseId = ((Number) presentData.get("base_id")).longValue();
        Item baseItem = game.getItemManager().getItem(baseId);
        
        if (baseItem == null) {
            return;
        }
        
        // Remove present from room
        room.removeFurniture(client, itemId);
        
        // Send present opened message (ID 219)
        ServerMessage openedMsg = new ServerMessage(219);
        openedMsg.appendUInt(itemId);
        client.sendMessage(openedMsg);
        
        // Send item info (ID 129)
        ServerMessage itemInfoMsg = new ServerMessage(129);
        itemInfoMsg.appendStringWithBreak(baseItem.getType());
        itemInfoMsg.appendInt32(baseItem.getSpriteId());
        itemInfoMsg.appendStringWithBreak(baseItem.getItemName());
        client.sendMessage(itemInfoMsg);
        
        // Delete present from database
        game.getInventoryRepository().deleteUserPresent(itemId);
        
        // Deliver items to user
        int amount = ((Number) presentData.get("amount")).intValue();
        String extraData = (String) presentData.get("extra_data");
        game.getCatalog().deliverItems(client, baseItem, amount, extraData != null ? extraData : "");
    }
}
