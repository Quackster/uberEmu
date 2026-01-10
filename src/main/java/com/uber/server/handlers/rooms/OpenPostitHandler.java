package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.items.RoomItem;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for opening a postit (message ID 83).
 * Ported from Messages/Requests/Rooms.cs OpenPostit()
 */
public class OpenPostitHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(OpenPostitHandler.class);
    private final Game game;
    
    public OpenPostitHandler(Game game) {
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
        
        ServerMessage response = new ServerMessage(48);
        response.appendStringWithBreak(String.valueOf(item.getId()));
        response.appendStringWithBreak(item.getExtraData() != null ? item.getExtraData() : "");
        client.sendMessage(response);
    }
}
