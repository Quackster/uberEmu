package com.uber.server.handlers.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for calling guide bot (message ID 440).
 * Ported from Messages/Requests/Help.cs CallGuideBot()
 * Note: This is a placeholder implementation until BotManager is fully ported.
 */
public class CallGuideBotHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(CallGuideBotHandler.class);
    private final Game game;
    
    public CallGuideBotHandler(Game game) {
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
        
        // Get guide bot (bot ID 55)
        com.uber.server.game.bots.RoomBot guideBot = game.getBotManager().getBot(55);
        if (guideBot == null) {
            logger.warn("Guide bot (ID 55) not found in database");
            return;
        }
        
        // Check if guide bot already exists in room
        for (com.uber.server.game.rooms.RoomUser roomUser : room.getUsers().values()) {
            if (roomUser.isBot() && roomUser.getBotData() != null && roomUser.getBotData().getBotId() == 55) {
                ServerMessage response = new ServerMessage(33);
                response.appendInt32(4009); // Error code: guide bot already exists
                client.sendMessage(response);
                return;
            }
        }
        
        // Check if user already called guide bot
        if (habbo.isCalledGuideBot()) {
            ServerMessage response = new ServerMessage(33);
            response.appendInt32(4010); // Error code: user already called guide bot
            client.sendMessage(response);
            return;
        }
        
        // Deploy guide bot
        com.uber.server.game.rooms.RoomUser botUser = room.deployBot(guideBot);
        if (botUser == null) {
            logger.warn("Failed to deploy guide bot in room {}", room.getRoomId());
            return;
        }
        
        // Move bot to room owner position
        com.uber.server.game.rooms.RoomUser roomOwner = room.getRoomUserByHabbo(room.getData().getOwner());
        if (roomOwner != null) {
            botUser.moveTo(roomOwner.getX(), roomOwner.getY());
            botUser.setRot(com.uber.server.game.pathfinding.Rotation.calculate(
                botUser.getX(), botUser.getY(), roomOwner.getX(), roomOwner.getY()));
            botUser.setUpdateNeeded(true);
        }
        
        // Unlock achievement 6.1
        if (game.getAchievementManager() != null) {
            game.getAchievementManager().unlockAchievement(client, 6, 1);
        }
        
        // Set called guide bot flag
        habbo.setCalledGuideBot(true);
    }
}
