package com.uber.server.messages.incoming.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GiveRightsMessageComposer (ID 96).
 * Processes room rights granting requests from the client.
 */
public class GiveRightsMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GiveRightsMessageComposerHandler.class);
    private final Game game;
    
    public GiveRightsMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long userId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.GiveRightsEvent event = new com.uber.server.event.packet.room.GiveRightsEvent(client, message, userId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        userId = event.getUserId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        com.uber.server.game.rooms.RoomUser roomUser = room.getRoomUserByHabbo(userId);
        if (roomUser == null || roomUser.isBot()) {
            return;
        }
        
        if (room.hasRights(userId)) {
            client.sendNotif("User already has rights! (There appears to be a bug with the rights button, we are looking into it - for now rely on 'Advanced settings')");
            return;
        }
        
        if (room.addRight(userId)) {
            // Get username
            String username = "";
            GameClient userClient = game.getClientManager().getClientByHabbo(userId);
            if (userClient != null && userClient.getHabbo() != null) {
                username = userClient.getHabbo().getUsername();
            } else {
                username = game.getUserRepository().getRealName(userId);
                if (username == null) {
                    username = "";
                }
            }
            
            var rightsGivenComposer = new com.uber.server.messages.outgoing.rooms.FlatControllerAddedComposer(room.getRoomId(), userId, username);
            client.sendMessage(rightsGivenComposer.compose());
            
            // Add status to room user
            roomUser.addStatus("roomControl", "");
            roomUser.setUpdateNeeded(true);
            
            // Notify user
            if (userClient != null) {
                var rightsGivenToUserComposer = new com.uber.server.messages.outgoing.rooms.RoomRightsGivenToUserMessageEventComposer();
                userClient.sendMessage(rightsGivenToUserComposer.compose());
            }
        }
    }
}
