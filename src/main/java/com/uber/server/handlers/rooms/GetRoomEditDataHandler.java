package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting room edit data (message ID 400).
 */
public class GetRoomEditDataHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetRoomEditDataHandler.class);
    private final Game game;
    
    public GetRoomEditDataHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int roomId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.GetRoomEditDataEvent event = new com.uber.server.event.packet.room.GetRoomEditDataEvent(client, message, roomId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        roomId = event.getRoomId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        com.uber.server.game.rooms.RoomData data = room.getData();
        if (data == null) {
            return;
        }
        
        ServerMessage response = new ServerMessage(465);
        response.appendUInt(room.getRoomId());
        response.appendStringWithBreak(data.getName());
        response.appendStringWithBreak(data.getDescription());
        response.appendInt32(data.getState());
        response.appendInt32(data.getCategory());
        response.appendInt32(data.getUsersMax());
        response.appendInt32(25); // Max users limit (hardcoded in original)
        response.appendInt32(data.getTagCount());
        
        for (String tag : data.getTags()) {
            response.appendStringWithBreak(tag);
        }
        
        response.appendInt32(room.getUsersWithRights().size()); // Users with rights count
        
        for (Long userId : room.getUsersWithRights()) {
            response.appendUInt(userId);
            // Get username
            String username = "";
            com.uber.server.game.GameClient userClient = game.getClientManager().getClientByHabbo(userId);
            if (userClient != null && userClient.getHabbo() != null) {
                username = userClient.getHabbo().getUsername();
            } else {
                username = game.getUserRepository().getRealName(userId);
                if (username == null) {
                    username = "";
                }
            }
            response.appendStringWithBreak(username);
        }
        
        response.appendInt32(room.getUsersWithRights().size()); // Users with rights count (duplicate)
        response.appendBoolean(data.isAllowPets()); // Allows pets in room
        response.appendBoolean(data.isAllowPetsEating()); // Allows pets to eat your food
        response.appendBoolean(data.isAllowWalkthrough());
        
        client.sendMessage(response);
    }
}
