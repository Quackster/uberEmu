package com.uber.server.messages.incoming.messenger;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for FollowFriendMessageComposer (ID 262).
 * Processes follow friend requests from the client.
 */
public class FollowFriendMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(FollowFriendMessageComposerHandler.class);
    private final Game game;
    
    public FollowFriendMessageComposerHandler(Game game) {
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
        var buddyClient = game.getClientManager().getClientByHabbo(buddyId);
        
        if (buddyClient == null || buddyClient.getHabbo() == null || !buddyClient.getHabbo().isInRoom()) {
            return;
        }
        
        var room = game.getRoomManager().getRoom(buddyClient.getHabbo().getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        var composer = new com.uber.server.messages.outgoing.navigator.RoomForwardComposer(
            room.getData() != null && room.getData().isPublicRoom(),
            buddyClient.getHabbo().getCurrentRoomId());
        client.sendMessage(composer.compose());
        
        // If not public room, prepare room entry
        if (room.getData() != null && !room.getData().isPublicRoom()) {
            // Use OpenConnectionMessageComposerHandler's prepareRoomForUser method
            var enterHandler = new com.uber.server.messages.incoming.rooms.OpenConnectionMessageComposerHandler(game);
            enterHandler.prepareRoomForUser(client, habbo, buddyClient.getHabbo().getCurrentRoomId(), "");
        }
    }
}
