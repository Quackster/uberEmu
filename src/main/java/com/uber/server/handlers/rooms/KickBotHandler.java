package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for kicking a bot (message ID 441).
 */
public class KickBotHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(KickBotHandler.class);
    private final Game game;
    
    public KickBotHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        int botId = message.popWiredInt32();
        
        com.uber.server.event.packet.room.KickBotEvent event = new com.uber.server.event.packet.room.KickBotEvent(client, message, botId);
        com.uber.server.game.Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        botId = event.getBotId();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null || !room.checkRights(client, true)) {
            return;
        }
        
        int virtualId = botId;
        com.uber.server.game.rooms.RoomUser botUser = room.getRoomUserByVirtualId(virtualId);
        
        if (botUser == null || !botUser.isBot()) {
            return;
        }
        
        // Remove bot from room (kicked = true)
        room.removeBot(virtualId, true);
    }
}
