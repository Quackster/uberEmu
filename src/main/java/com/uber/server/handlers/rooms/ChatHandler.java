package com.uber.server.handlers.rooms;

import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.PacketHandler;
import com.uber.server.messages.ServerMessage;
import com.uber.server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Handler for chat messages (message IDs 52 = Talk, 55 = Shout, 56 = Whisper).
 * Ported from Messages/Requests/Rooms.cs Talk(), Shout(), Whisper()
 */
public class ChatHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatHandler.class);
    private final Game game;
    private final int chatType; // 0 = talk, 1 = shout, 2 = whisper
    
    public ChatHandler(Game game, int chatType) {
        this.game = game;
        this.chatType = chatType;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null) {
            return;
        }
        
        if (!habbo.isInRoom()) {
            return;
        }
        
        String chatMessage = message.popFixedString();
        if (chatMessage == null || chatMessage.isEmpty()) {
            return;
        }
        
        // Filter injection characters and trim
        chatMessage = StringUtil.filterInjectionChars(chatMessage, true); // Allow linebreaks for chat
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
        
        // Send chat message to room
        if (game.getRoomManager() != null) {
            com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
            if (room != null) {
                com.uber.server.game.rooms.RoomUser roomUser = room.getRoomUserByHabbo(habbo.getId());
                if (roomUser != null) {
                    boolean shout = (chatType == 1); // 1 = shout
                    roomUser.chat(client, chatMessage, shout);
                }
            }
        }
        
        logger.debug("Chat from {} in room {}: {}", habbo.getUsername(), habbo.getCurrentRoomId(), chatMessage);
    }
}
