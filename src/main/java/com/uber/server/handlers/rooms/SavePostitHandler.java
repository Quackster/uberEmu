package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for saving a postit (message ID 84).
 */
public class SavePostitHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SavePostitHandler.class);
    private final Game game;
    
    public SavePostitHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long itemId = message.popWiredUInt();
        String postitText = message.popFixedString();
        String postitColor = message.popFixedString();
        
        com.uber.server.event.packet.room.SavePostitEvent event = new com.uber.server.event.packet.room.SavePostitEvent(client, message, itemId, postitText, postitColor);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        itemId = event.getItemId();
        postitText = event.getText();
        postitColor = event.getColor();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        RoomItem item = room.getItem(itemId);
        
        if (item == null) {
            return;
        }
        
        com.uber.server.game.items.Item baseItem = item.getBaseItem();
        if (baseItem == null || !"postit".equalsIgnoreCase(baseItem.getInteractionType())) {
            return;
        }
        
        if (postitText == null) {
            postitText = "";
        }
        String text = StringUtil.filterInjectionChars(postitText, true);
        String color = postitColor;
        
        // Check if user has rights (if not, can only append to existing text)
        if (!room.checkRights(client)) {
            String existingData = item.getExtraData();
            if (existingData != null && !data.startsWith(existingData)) {
                return; // Can only add to existing text, not modify
            }
        }
        
        // Validate color
        boolean isValidColor = switch (color) {
            case "FFFF33", "FF9CFF", "9CCEFF", "9CFF9C" -> true;
            default -> false;
        };
        if (!isValidColor) {
            return; // Invalid color
        }
        
        // Update postit
        item.setExtraData(color + " " + text);
        item.updateState(true, true);
    }
}
