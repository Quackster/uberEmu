package com.uber.server.handlers.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for following a buddy (message ID 262).
 */
public class FollowBuddyHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(FollowBuddyHandler.class);
    private final Game game;
    
    public FollowBuddyHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long buddyId = message.popWiredUInt();
        
        com.uber.server.event.packet.messenger.FollowFriendEvent event = new com.uber.server.event.packet.messenger.FollowFriendEvent(client, message, buddyId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        buddyId = event.getUserId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        GameClient buddyClient = game.getClientManager().getClientByHabbo(buddyId);
        
        if (buddyClient == null || buddyClient.getHabbo() == null || !buddyClient.getHabbo().isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(buddyClient.getHabbo().getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        ServerMessage response = new ServerMessage(286);
        response.appendBoolean(room.getData() != null && room.getData().isPublicRoom());
        response.appendUInt(buddyClient.getHabbo().getCurrentRoomId());
        client.sendMessage(response);
        
        // If not public room, prepare room entry
        if (room.getData() != null && !room.getData().isPublicRoom()) {
            // Use OpenConnectionMessageComposerHandler's prepareRoomForUser method
            var enterHandler = new com.uber.server.messages.incoming.rooms.OpenConnectionMessageComposerHandler(game);
            enterHandler.prepareRoomForUser(client, habbo, buddyClient.getHabbo().getCurrentRoomId(), "");
        }
    }
}
