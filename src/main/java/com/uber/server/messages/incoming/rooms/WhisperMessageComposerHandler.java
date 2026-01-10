package com.uber.server.messages.incoming.rooms;

import com.uber.server.event.packet.room.WhisperMessageEvent;
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
 * Handler for WhisperMessageComposer (ID 56).
 * Processes whisper messages from the client.
 */
public class WhisperMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(WhisperMessageComposerHandler.class);
    private final Game game;
    
    public WhisperMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        String targetUsername = message.popFixedString();
        String chatMessage = message.popFixedString();
        
        WhisperMessageEvent event = new WhisperMessageEvent(client, message, chatMessage, targetUsername);
        Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // Use event fields instead of local variables
        chatMessage = event.getMessage();
        targetUsername = event.getTargetUsername();
        
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        if (!habbo.isInRoom()) {
            return;
        }
        
        if (chatMessage == null || chatMessage.isEmpty() || targetUsername == null) {
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
        
        // Send whisper message to room (target user will be handled by room logic)
        if (game.getRoomManager() != null) {
            var room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
            if (room != null) {
                var roomUser = room.getRoomUserByHabbo(habbo.getId());
                if (roomUser != null) {
                    roomUser.chat(client, chatMessage, 2); // 2 = whisper
                }
            }
        }
        
        logger.debug("Whisper from {} to {} in room {}: {}", habbo.getUsername(), targetUsername, habbo.getCurrentRoomId(), chatMessage);
    }
}
