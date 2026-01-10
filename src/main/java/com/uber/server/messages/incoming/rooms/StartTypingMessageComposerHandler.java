package com.uber.server.messages.incoming.rooms;

import com.uber.server.event.packet.room.StartTypingEvent;
import com.uber.server.game.Game;
import com.uber.server.game.GameClient;
import com.uber.server.game.Habbo;
import com.uber.server.messages.ClientMessage;
import com.uber.server.messages.incoming.IncomingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for StartTypingMessageComposer (ID 361).
 * Processes typing start notifications from the client.
 */
public class StartTypingMessageComposerHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(StartTypingMessageComposerHandler.class);
    private final Game game;
    
    public StartTypingMessageComposerHandler(Game game) {
        this.game = game;
    }
    
    @Override
    public void handle(GameClient client, ClientMessage message) {
        message.resetPointer();
        
        StartTypingEvent event = new StartTypingEvent(client, message);
        Game.getInstance().getEventManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
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
        
        var composer = new com.uber.server.messages.outgoing.rooms.UserTypingMessageComposer(
            roomUser.getVirtualId(), true);
        room.sendMessage(composer.compose());
    }
}
