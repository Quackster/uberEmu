package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Handler for TakeAllRightsMessageComposer (ID 155).
 * Processes remove all room rights requests from the client.
 */
public class TakeAllRightsMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(TakeAllRightsMessageComposerHandler.class);
    private final Game game;
    
    public TakeAllRightsMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.room.TakeAllRightsEvent event = new com.uber.server.event.packet.room.TakeAllRightsEvent(client, message);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        // Notify all users with rights
        for (Long userId : new ArrayList<>(room.getUsersWithRights())) {
            com.uber.server.game.rooms.RoomUser user = room.getRoomUserByHabbo(userId);
            if (user != null && !user.isBot()) {
                GameClient userClient = user.getClient();
                if (userClient != null) {
                    var rightsRemovedComposer = new com.uber.server.messages.outgoing.rooms.YouAreNotControllerComposer();
                    userClient.sendMessage(rightsRemovedComposer.compose());
                }
            }
            
            var rightsRemovedComposer = new com.uber.server.messages.outgoing.rooms.FlatControllerRemovedComposer(room.getRoomId(), userId);
            client.sendMessage(rightsRemovedComposer.compose());
        }
        
        // Delete all rights from database
        if (game.getRoomRepository().deleteRoomRights(room.getRoomId(), null)) {
            room.removeAllRights();
        }
    }
}
