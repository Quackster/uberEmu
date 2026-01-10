package com.uber.server.messages.incoming.users;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for GetUserTagsMessageComposer (ID 263).
 * Processes user tags requests from the client.
 */
public class GetUserTagsMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetUserTagsMessageComposerHandler.class);
    private final Game game;
    
    public GetUserTagsMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long targetUserId = message.popWiredUInt();
        
        com.uber.server.event.packet.user.GetUserTagsEvent event = new com.uber.server.event.packet.user.GetUserTagsEvent(client, message, targetUserId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        targetUserId = event.getUserId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        var room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        var targetUser = room.getRoomUserByHabbo(targetUserId);
        
        if (targetUser == null || targetUser.isBot()) {
            return;
        }
        
        var targetClient = targetUser.getClient();
        if (targetClient == null || targetClient.getHabbo() == null) {
            return;
        }
        
        // TODO: Replace with UserTagsMessageEventComposer (ID 350)
        ServerMessage response = new ServerMessage(350);
        response.appendUInt(targetClient.getHabbo().getId());
        response.appendInt32(targetClient.getHabbo().getTags().size());
        
        for (String tag : targetClient.getHabbo().getTags()) {
            response.appendStringWithBreak(tag);
        }
        
        client.sendMessage(response);
    }
}
