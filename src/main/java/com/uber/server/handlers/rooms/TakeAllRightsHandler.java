package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Handler for taking all room rights (message ID 155).
 * Ported from Messages/Requests/Rooms.cs TakeAllRights()
 */
public class TakeAllRightsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(TakeAllRightsHandler.class);
    private final Game game;
    
    public TakeAllRightsHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        // Notify all users with rights
        for (Long userId : new ArrayList<>(room.getUsersWithRights())) {
            com.uber.server.rooms.RoomUser user = room.getRoomUserByHabbo(userId);
            if (user != null && !user.isBot()) {
                GameClient userClient = user.getClient();
                if (userClient != null) {
                    userClient.sendMessage(new ServerMessage(43));
                }
            }
            
            ServerMessage response = new ServerMessage(511);
            response.appendUInt(room.getRoomId());
            response.appendUInt(userId);
            client.sendMessage(response);
        }
        
        // Delete all rights from database
        if (game.getRoomRepository().deleteRoomRights(room.getRoomId(), null)) {
            room.removeAllRights();
        }
    }
}
