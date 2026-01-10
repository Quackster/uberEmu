package com.uber.server.messages.incoming.help;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for CallGuideBotMessageComposer (ID 440).
 * Processes guide bot call requests from the client.
 */
public class CallGuideBotMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(CallGuideBotMessageComposerHandler.class);
    private final Game game;
    
    public CallGuideBotMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        com.uber.server.event.packet.help.CallGuideBotEvent event = new com.uber.server.event.packet.help.CallGuideBotEvent(client, message);
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
        
        // Get guide bot (bot ID 55)
        com.uber.server.game.bots.RoomBot guideBot = game.getBotManager().getBot(55);
        if (guideBot == null) {
            logger.warn("Guide bot (ID 55) not found in database");
            return;
        }
        
        // Check if guide bot already exists in room
        for (com.uber.server.game.rooms.RoomUser roomUser : room.getUsers().values()) {
            if (roomUser.isBot() && roomUser.getBotData() != null && roomUser.getBotData().getBotId() == 55) {
                var errorComposer = new com.uber.server.messages.outgoing.global.GenericErrorComposer(4009); // Error code: guide bot already exists
                client.sendMessage(errorComposer.compose());
                return;
            }
        }
        
        // Check if user already called guide bot
        if (habbo.isCalledGuideBot()) {
            var errorComposer = new com.uber.server.messages.outgoing.global.GenericErrorComposer(4010);
            client.sendMessage(errorComposer.compose());
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
