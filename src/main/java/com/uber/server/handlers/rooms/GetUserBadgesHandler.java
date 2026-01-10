package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.game.users.badges.Badge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for getting user badges (message ID 159).
 */
public class GetUserBadgesHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetUserBadgesHandler.class);
    private final Game game;
    
    public GetUserBadgesHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long targetUserId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.GetUserBadgesEvent event = new com.uber.server.event.packet.room.GetUserBadgesEvent(client, message, targetUserId);
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
        
        ServerMessage response = new ServerMessage(228);
        response.appendUInt(targetClient.getHabbo().getId());
        response.appendInt32(targetClient.getHabbo().getBadgeComponent().getEquippedCount());
        
        for (Badge badge : targetClient.getHabbo().getBadgeComponent().getBadgeList()) {
            if (badge.getSlot() > 0) {
                response.appendInt32(badge.getSlot());
                response.appendStringWithBreak(badge.getCode());
            }
        }
        
        var badgesComposer = new com.uber.server.messages.outgoing.users.HabboUserBadgesComposer(response);
        client.sendMessage(badgesComposer.compose());
    }
}
