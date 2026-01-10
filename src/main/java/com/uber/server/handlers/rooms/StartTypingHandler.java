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
 * Handler for starting typing (message ID 317).
 * Ported from Messages/Requests/Rooms.cs StartTyping()
 */
public class StartTypingHandler implements PacketHandler {
    private static final Logger logger = LoggerFactory.getLogger(StartTypingHandler.class);
    private final Game game;
    
    public StartTypingHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        Habbo habbo = client.getHabbo();
        if (habbo == null || !habbo.isInRoom()) {
            return;
        }
        
        com.uber.server.game.rooms.Room room = game.getRoomManager().getRoom(habbo.getCurrentRoomId());
        if (room == null) {
            return;
        }
        
        com.uber.server.game.rooms.RoomUser roomUser = room.getRoomUserByHabbo(habbo.getId());
        if (roomUser == null) {
            return;
        }
        
        ServerMessage typingMessage = new ServerMessage(361);
        typingMessage.appendInt32(roomUser.getVirtualId());
        typingMessage.appendBoolean(true);
        room.sendMessage(typingMessage);
    }
}
