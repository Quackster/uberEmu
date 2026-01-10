package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for TakeRightsMessageComposer (ID 97).
 * Processes room rights removal requests from the client.
 */
public class TakeRightsMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(TakeRightsMessageComposerHandler.class);
    private final Game game;
    
    public TakeRightsMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
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
            
            com.uber.server.game.rooms.RoomUser user = room.getRoomUserByHabbo(userIdsArray[i]);
            if (user != null && !user.isBot()) {
                GameClient userClient = user.getClient();
                if (userClient != null) {
                    var rightsRemovedComposer = new com.uber.server.messages.outgoing.rooms.RoomRightsRemovedFromUserMessageEventComposer();
                    userClient.sendMessage(rightsRemovedComposer.compose());
                }
            }
            
            var rightsRemovedComposer = new com.uber.server.messages.outgoing.rooms.RoomRightsRemovedMessageEventComposer(room.getRoomId(), userIdsArray[i]);
            client.sendMessage(rightsRemovedComposer.compose());
        }
        
        // Delete from database
        game.getRoomRepository().deleteRoomRights(room.getRoomId(), userIdsArray);
    }
}
