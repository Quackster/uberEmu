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
 * Handler for getting user tags (message ID 263).
 */
public class GetUserTagsHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetUserTagsHandler.class);
    private final Game game;
    
    public GetUserTagsHandler(Game game) {
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
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        com.uber.server.game.rooms.RoomUser targetUser = room.getRoomUserByHabbo(targetUserId);
        
        if (targetUser == null || targetUser.isBot()) {
            return;
        }
        
        GameClient targetClient = targetUser.getClient();
        if (targetClient == null || targetClient.getHabbo() == null) {
            return;
        }
        
        ServerMessage response = new ServerMessage(350);
        response.appendUInt(targetClient.getHabbo().getId());
        response.appendInt32(targetClient.getHabbo().getTags().size());
        
        for (String tag : targetClient.getHabbo().getTags()) {
            response.appendStringWithBreak(tag);
        }
        
        client.sendMessage(response);
    }
}
