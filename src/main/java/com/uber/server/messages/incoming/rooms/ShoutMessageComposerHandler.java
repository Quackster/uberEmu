package com.uber.server.messages.incoming.rooms;

import com.uber.server.event.packet.room.ShoutMessageEvent;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Handler for ShoutMessageComposer (ID 55).
 * Processes shout messages from the client.
 */
public class ShoutMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ShoutMessageComposerHandler.class);
    private final Game game;
    
    public ShoutMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String chatMessage = message.popFixedString();
        
        ShoutMessageEvent event = new ShoutMessageEvent(client, message, chatMessage);
        Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event field instead of local variable
        chatMessage = event.getMessage();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        if (!habbo.isInRoom()) {
            return;
        }
        if (chatMessage == null || chatMessage.isEmpty()) {
            return;
        }
        
        // Filter injection characters and trim
        chatMessage = StringUtil.filterInjectionChars(chatMessage, true);
        chatMessage = chatMessage.trim();
        if (chatMessage.length() > 100) {
            chatMessage = chatMessage.substring(0, 100);
        }
        
        // Log chat message
        LocalDateTime now = LocalDateTime.now();
        long timestamp = System.currentTimeMillis() / 1000;
        game.getChatLogRepository().logChat(habbo.getId(), habbo.getCurrentRoomId(),
                now.getHour(), now.getMinute(), timestamp, chatMessage,
                habbo.getUsername(), now.toLocalDate().toString());
        
        // Send shout message to room
        if (game.getRoomManager() != null) {
            var room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
            if (room != null) {
                var roomUser = room.getRoomUserByHabbo(habbo.getId());
                if (roomUser != null) {
                    roomUser.chat(client, chatMessage, 1); // 1 = shout
                }
            }
        }
        
        logger.debug("Shout from {} in room {}: {}", habbo.getUsername(), habbo.getCurrentRoomId(), chatMessage);
    }
}
