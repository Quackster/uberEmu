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
 * Handler for giving respect (message ID 371).
 */
public class GiveRespectHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GiveRespectHandler.class);
    private final Game game;
    
    public GiveRespectHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        long targetUserId = message.popWiredUInt();
        
        com.uber.server.event.packet.room.GiveRespectEvent event = new com.uber.server.event.packet.room.GiveRespectEvent(client, message, targetUserId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        targetUserId = event.getUserId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom() || habbo.getDailyRespectPoints() <= 0) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        com.uber.server.game.rooms.RoomUser targetUser = room.getRoomUserByHabbo(targetUserId);
        
        if (targetUser == null || targetUser.isBot() || targetUser.getHabboId() == habbo.getId()) {
            return;
        }
        
        GameClient targetClient = targetUser.getClient();
        if (targetClient == null || targetClient.getHabbo() == null) {
            return;
        }
        
        // Decrement daily respect points
        habbo.setDailyRespectPoints(habbo.getDailyRespectPoints() - 1);
        
        // Increment target's respect
        Habbo targetHabbo = targetClient.getHabbo();
        targetHabbo.setRespect(targetHabbo.getRespect() + 1);
        
        // Update database
        game.getUserRepository().updateRespect(targetHabbo.getId(), targetHabbo.getRespect());
        game.getUserRepository().updateDailyRespectPoints(habbo.getId(), habbo.getDailyRespectPoints());
        
        // Send respect message
        ServerMessage respectMessage = new ServerMessage(440);
        respectMessage.appendUInt(targetHabbo.getId());
        respectMessage.appendInt32(targetHabbo.getRespect());
        room.sendMessage(respectMessage);
    }
}
