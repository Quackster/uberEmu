package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.game.items.Item;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.users.inventory.UserItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for applying room effect (wallpaper/floor/landscape) (message ID 66).
 */
public class ApplyRoomEffectHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApplyRoomEffectHandler.class);
    private final Game game;
    
    public ApplyRoomEffectHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int effectId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.ApplyRoomEffectEvent event = new com.uber.server.event.packet.room.ApplyRoomEffectEvent(client, message, effectId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        effectId = event.getEffectId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        // Note: Handler logic uses itemId, but event has effectId
        // Keeping original logic but using effectId as itemId for compatibility
        long itemId = effectId;
        UserItem item = habbo.getInventoryComponent().getItem(itemId);
        
        if (item == null) {
            return;
        }
        
        Item baseItem = item.getBaseItem();
        if (baseItem == null) {
            return;
        }
        
        String type = "floor";
        String itemName = baseItem.getItemName().toLowerCase();
        
        if (itemName.contains("wallpaper")) {
            type = "wallpaper";
        } else if (itemName.contains("landscape")) {
            type = "landscape";
        }
        
        // Update room data
        com.uber.server.game.rooms.RoomData data = room.getData();
        String extraData = item.getExtraData() != null ? item.getExtraData() : "";
        
        switch (type) {
            case "floor":
                data.setFloor(extraData);
                break;
            case "wallpaper":
                data.setWallpaper(extraData);
                break;
            case "landscape":
                data.setLandscape(extraData);
                break;
        }
        
        // Update database
        game.getRoomRepository().updateRoomDecoration(room.getRoomId(), type, extraData);
        
        // Remove item from inventory
        habbo.getInventoryComponent().removeItem(itemId);
        
        // Broadcast room effect update
        ServerMessage effectMsg = new ServerMessage(46);
        effectMsg.appendStringWithBreak(type);
        effectMsg.appendStringWithBreak(extraData);
        room.sendMessage(effectMsg);
    }
}
