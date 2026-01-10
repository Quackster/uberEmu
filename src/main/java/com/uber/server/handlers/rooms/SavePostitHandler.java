package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for saving a postit (message ID 84).
 * Ported from Messages/Requests/Rooms.cs SavePostit()
 */
public class SavePostitHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SavePostitHandler.class);
    private final Game game;
    
    public SavePostitHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        long itemId = message.popWiredUInt();
        RoomItem item = room.getItem(itemId);
        
        if (item == null) {
            return;
        }
        
        com.uber.server.items.Item baseItem = item.getBaseItem();
        if (baseItem == null || !"postit".equalsIgnoreCase(baseItem.getInteractionType())) {
            return;
        }
        
        String data = message.popFixedString();
        if (data == null || data.isEmpty()) {
            return;
        }
        
        // Parse color and text
        String[] parts = data.split(" ", 2);
        if (parts.length < 2) {
            return;
        }
        
        String color = parts[0];
        String text = parts.length > 1 ? StringUtil.filterInjectionChars(parts[1], true) : "";
        
        // Check if user has rights (if not, can only append to existing text)
        if (!room.checkRights(client)) {
            String existingData = item.getExtraData();
            if (existingData != null && !data.startsWith(existingData)) {
                return; // Can only add to existing text, not modify
            }
        }
        
        // Validate color
        switch (color) {
            case "FFFF33":
            case "FF9CFF":
            case "9CCEFF":
            case "9CFF9C":
                break;
            default:
                return; // Invalid color
        }
        
        // Update postit
        item.setExtraData(color + " " + text);
        item.updateState(true, true);
    }
}
