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
import java.util.List;

/**
 * Handler for taking room rights (message ID 97).
 * Ported from Messages/Requests/Rooms.cs TakeRights()
 */
public class TakeRightsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(TakeRightsHandler.class);
    private final Game game;
    
    public TakeRightsHandler(Game game) {
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
        
        int amount = message.popWiredInt32();
        List<Long> userIdsToRemove = new ArrayList<>();
        
        for (int i = 0; i < amount; i++) {
            long userId = message.popWiredUInt();
            userIdsToRemove.add(userId);
        }
        
        // Remove rights for each user
        long[] userIdsArray = new long[userIdsToRemove.size()];
        for (int i = 0; i < userIdsToRemove.size(); i++) {
            userIdsArray[i] = userIdsToRemove.get(i);
            room.removeRight(userIdsArray[i]);
            
            com.uber.server.rooms.RoomUser user = room.getRoomUserByHabbo(userIdsArray[i]);
            if (user != null && !user.isBot()) {
                GameClient userClient = user.getClient();
                if (userClient != null) {
                    userClient.sendMessage(new ServerMessage(43));
                }
            }
            
            ServerMessage response = new ServerMessage(511);
            response.appendUInt(room.getRoomId());
            response.appendUInt(userIdsArray[i]);
            client.sendMessage(response);
        }
        
        // Delete from database
        game.getRoomRepository().deleteRoomRights(room.getRoomId(), userIdsArray);
    }
}
